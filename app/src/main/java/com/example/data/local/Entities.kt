package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerKey: String,
    val vegetableName: String,
    val ratePerKg: Double,
    val weightKg: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val udhaarAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "customer_udhaar")
data class CustomerUdhaar(
    @PrimaryKey val customerKey: String,
    val displayName: String,
    val pendingAmount: Double,
    val lastUpdated: Long = System.currentTimeMillis(),
)

@Entity(tableName = "roz_hisab")
data class RozHisab(
    @PrimaryKey val id: Int = ACTIVE_DAY_ID,
    val dateKey: String,
    val totalSale: Double = 0.0,
    val totalCashMila: Double = 0.0,
    val totalUdhaar: Double = 0.0,
    val gaariKharcha: Double = 0.0,
    val mazdooriKharcha: Double = 0.0,
    val otherKharcha: Double = 0.0,
    val totalKharcha: Double = 0.0,
    val munafa: Double = 0.0,
    val saleCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
) {
    companion object {
        const val ACTIVE_DAY_ID = 1
    }
}

@Entity(tableName = "vegetable_rates")
data class VegetableRateEntity(
    @PrimaryKey val name: String,
    val ratePerKg: Double,
    val sortOrder: Int,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val note: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
