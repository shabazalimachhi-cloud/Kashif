package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.CustomerUdhaar
import com.example.data.local.ExpenseEntity
import com.example.data.local.MandiDatabase
import com.example.data.local.RozHisab
import com.example.data.local.Sale
import com.example.data.local.VegetableRateEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

class MandiRepository(private val db: MandiDatabase) {

    private val vegetableDao = db.vegetableRateDao()
    private val saleDao = db.saleDao()
    private val udhaarDao = db.customerUdhaarDao()
    private val rozHisabDao = db.rozHisabDao()
    private val expenseDao = db.expenseDao()

    val vegetables: Flow<List<VegetableRateEntity>> = vegetableDao.getAllVegetables()
    val sales: Flow<List<Sale>> = saleDao.getAllSales()
    val pendingUdhaarCustomers: Flow<List<CustomerUdhaar>> =
        udhaarDao.getPendingUdhaarCustomers()
    val recentCustomerNames: Flow<List<String>> = udhaarDao.getRecentCustomerNames()
    val activeRozHisab: Flow<RozHisab?> = rozHisabDao.observeRozHisab()
    val expenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun ensureDefaultVegetablesSeeded() {
        db.withTransaction {
            if (vegetableDao.getCount() == 0) {
                vegetableDao.insertAllIgnore(DEFAULT_VEGETABLES)
            }
            if (rozHisabDao.getRozHisab() == null) {
                syncRozHisabLocked()
            }
        }
    }

    suspend fun updateVegetableRate(name: String, newRate: Double) {
        if (newRate >= 0.0) {
            vegetableDao.updateRate(name, roundCurrency(newRate))
        }
    }

    suspend fun addSale(
        customerName: String,
        vegetableName: String,
        ratePerKg: Double,
        weightKg: Double,
        paidInput: Double,
    ) {
        val cleanName = customerName.trim().ifEmpty { "Naqad Customer" }
        val customerKey = normalizeCustomerKey(cleanName)
        val total = roundCurrency(ratePerKg * weightKg)
        val appliedPaid = paidInput.coerceIn(0.0, total)
        val udhaar = roundCurrency((total - appliedPaid).coerceAtLeast(0.0))
        val now = System.currentTimeMillis()

        db.withTransaction {
            vegetableDao.updateRate(vegetableName, roundCurrency(ratePerKg))
            saleDao.insertSale(
                Sale(
                    customerName = cleanName,
                    customerKey = customerKey,
                    vegetableName = vegetableName,
                    ratePerKg = roundCurrency(ratePerKg),
                    weightKg = weightKg,
                    totalAmount = total,
                    paidAmount = roundCurrency(appliedPaid),
                    udhaarAmount = udhaar,
                    timestamp = now,
                ),
            )
            if (udhaar > 0.0) {
                val existing = udhaarDao.getCustomerByKey(customerKey)
                val updatedAmount = roundCurrency((existing?.pendingAmount ?: 0.0) + udhaar)
                udhaarDao.upsertCustomer(
                    CustomerUdhaar(
                        customerKey = customerKey,
                        displayName = cleanName,
                        pendingAmount = updatedAmount,
                        lastUpdated = now,
                    ),
                )
            }
            syncRozHisabLocked(now)
        }
    }

    suspend fun deleteSale(saleId: Long) {
        db.withTransaction {
            val sale = saleDao.getSaleById(saleId) ?: return@withTransaction
            saleDao.deleteSaleById(saleId)
            if (sale.udhaarAmount > 0.0) {
                val existing = udhaarDao.getCustomerByKey(sale.customerKey)
                if (existing != null) {
                    val remaining = roundCurrency((existing.pendingAmount - sale.udhaarAmount).coerceAtLeast(0.0))
                    udhaarDao.upsertCustomer(
                        existing.copy(
                            pendingAmount = remaining,
                            lastUpdated = System.currentTimeMillis(),
                        ),
                    )
                }
            }
            syncRozHisabLocked()
        }
    }

    suspend fun receiveUdhaarPayment(customerKey: String, amountReceived: Double) {
        if (amountReceived <= 0.0) return
        db.withTransaction {
            val existing = udhaarDao.getCustomerByKey(customerKey) ?: return@withTransaction
            val remaining = roundCurrency((existing.pendingAmount - amountReceived).coerceAtLeast(0.0))
            udhaarDao.upsertCustomer(
                existing.copy(
                    pendingAmount = remaining,
                    lastUpdated = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun addExpense(category: String, note: String, amount: Double) {
        if (amount <= 0.0) return
        val now = System.currentTimeMillis()
        db.withTransaction {
            expenseDao.insertExpense(
                ExpenseEntity(
                    category = category.trim().ifEmpty { "Kharcha" },
                    note = note.trim(),
                    amount = roundCurrency(amount),
                    timestamp = now,
                ),
            )
            syncRozHisabLocked(now)
        }
    }

    suspend fun deleteExpense(expenseId: Long) {
        db.withTransaction {
            expenseDao.deleteExpenseById(expenseId)
            syncRozHisabLocked()
        }
    }

    suspend fun endDay(clearUdhaarKhata: Boolean) {
        db.withTransaction {
            saleDao.clearAllSales()
            expenseDao.clearAllExpenses()
            if (clearUdhaarKhata) {
                udhaarDao.clearAllUdhaar()
            }
            syncRozHisabLocked()
        }
    }

    private suspend fun syncRozHisabLocked(now: Long = System.currentTimeMillis()) {
        val salesList = saleDao.getAllSalesSnapshot()
        val expensesList = expenseDao.getAllExpensesSnapshot()

        val totalSale = roundCurrency(salesList.sumOf { it.totalAmount })
        val totalCashMila = roundCurrency(salesList.sumOf { it.paidAmount })
        val totalUdhaar = roundCurrency(salesList.sumOf { it.udhaarAmount })

        val gaari = roundCurrency(
            expensesList.filter { it.category.equals("Gaari", ignoreCase = true) }.sumOf { it.amount },
        )
        val mazdoori = roundCurrency(
            expensesList.filter { it.category.equals("Mazdoori", ignoreCase = true) }.sumOf { it.amount },
        )
        val other = roundCurrency(
            expensesList.filter {
                !it.category.equals("Gaari", ignoreCase = true) &&
                    !it.category.equals("Mazdoori", ignoreCase = true)
            }.sumOf { it.amount },
        )
        val totalKharcha = roundCurrency(gaari + mazdoori + other)
        val munafa = roundCurrency(totalSale - totalKharcha)

        rozHisabDao.upsertRozHisab(
            RozHisab(
                id = RozHisab.ACTIVE_DAY_ID,
                dateKey = currentDateKey(now),
                totalSale = totalSale,
                totalCashMila = totalCashMila,
                totalUdhaar = totalUdhaar,
                gaariKharcha = gaari,
                mazdooriKharcha = mazdoori,
                otherKharcha = other,
                totalKharcha = totalKharcha,
                munafa = munafa,
                saleCount = salesList.size,
                lastUpdated = now,
            ),
        )
    }

    companion object {
        val DEFAULT_VEGETABLES = listOf(
            VegetableRateEntity(name = "Aloo", ratePerKg = 80.0, sortOrder = 0),
            VegetableRateEntity(name = "Pyaz", ratePerKg = 120.0, sortOrder = 1),
            VegetableRateEntity(name = "Tamatar", ratePerKg = 140.0, sortOrder = 2),
            VegetableRateEntity(name = "Adrak", ratePerKg = 480.0, sortOrder = 3),
            VegetableRateEntity(name = "Lehsan", ratePerKg = 420.0, sortOrder = 4),
            VegetableRateEntity(name = "Bhindi", ratePerKg = 160.0, sortOrder = 5),
            VegetableRateEntity(name = "Karela", ratePerKg = 150.0, sortOrder = 6),
            VegetableRateEntity(name = "Palak", ratePerKg = 70.0, sortOrder = 7),
            VegetableRateEntity(name = "Gobi", ratePerKg = 110.0, sortOrder = 8),
            VegetableRateEntity(name = "Baingan", ratePerKg = 100.0, sortOrder = 9),
        )

        fun normalizeCustomerKey(name: String): String {
            return name.trim().lowercase().replace(Regex("\\s+"), " ")
        }

        fun roundCurrency(value: Double): Double {
            return (value * 100.0).roundToLong() / 100.0
        }

        fun currentDateKey(timestamp: Long = System.currentTimeMillis()): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))
        }
    }
}
