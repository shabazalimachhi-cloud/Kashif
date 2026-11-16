package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Sale::class,
        CustomerUdhaar::class,
        RozHisab::class,
        VegetableRateEntity::class,
        ExpenseEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class MandiDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
    abstract fun customerUdhaarDao(): CustomerUdhaarDao
    abstract fun rozHisabDao(): RozHisabDao
    abstract fun vegetableRateDao(): VegetableRateDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: MandiDatabase? = null

        fun getInstance(context: Context): MandiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MandiDatabase::class.java,
                    "sabzi_mandi_khata.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
