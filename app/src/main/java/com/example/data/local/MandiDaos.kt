package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VegetableRateDao {
    @Query("SELECT * FROM vegetable_rates ORDER BY sortOrder ASC")
    fun getAllVegetables(): Flow<List<VegetableRateEntity>>

    @Query("SELECT COUNT(*) FROM vegetable_rates")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(items: List<VegetableRateEntity>)

    @Query("UPDATE vegetable_rates SET ratePerKg = :rate WHERE name = :name")
    suspend fun updateRate(name: String, rate: Double)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC, id DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesSnapshot(): List<Sale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): Sale?

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("DELETE FROM sales")
    suspend fun clearAllSales()
}

@Dao
interface CustomerUdhaarDao {
    @Query("SELECT * FROM customer_udhaar WHERE pendingAmount > 0.01 ORDER BY pendingAmount DESC, lastUpdated DESC")
    fun getPendingUdhaarCustomers(): Flow<List<CustomerUdhaar>>

    @Query("SELECT DISTINCT displayName FROM customer_udhaar ORDER BY lastUpdated DESC LIMIT 12")
    fun getRecentCustomerNames(): Flow<List<String>>

    @Query("SELECT * FROM customer_udhaar WHERE customerKey = :key LIMIT 1")
    suspend fun getCustomerByKey(key: String): CustomerUdhaar?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: CustomerUdhaar)

    @Query("DELETE FROM customer_udhaar")
    suspend fun clearAllUdhaar()
}

@Dao
interface RozHisabDao {
    @Query("SELECT * FROM roz_hisab WHERE id = :id LIMIT 1")
    fun observeRozHisab(id: Int = RozHisab.ACTIVE_DAY_ID): Flow<RozHisab?>

    @Query("SELECT * FROM roz_hisab WHERE id = :id LIMIT 1")
    suspend fun getRozHisab(id: Int = RozHisab.ACTIVE_DAY_ID): RozHisab?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRozHisab(rozHisab: RozHisab)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC, id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSnapshot(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}
