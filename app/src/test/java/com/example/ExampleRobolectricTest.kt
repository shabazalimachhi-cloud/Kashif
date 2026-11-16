package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MandiDatabase
import com.example.data.repository.MandiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: MandiDatabase
    private lateinit var repository: MandiRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, MandiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MandiRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name matches Sabzi Mandi Khata`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Sabzi Mandi Khata", appName)
    }

    @Test
    fun `default ten vegetables and initial RozHisab are seeded`() = runTest {
        repository.ensureDefaultVegetablesSeeded()
        val vegetables = repository.vegetables.first()
        assertEquals(10, vegetables.size)
        val names = vegetables.map { it.name }
        assertTrue(
            names.containsAll(
                listOf(
                    "Aloo",
                    "Pyaz",
                    "Tamatar",
                    "Adrak",
                    "Lehsan",
                    "Bhindi",
                    "Karela",
                    "Palak",
                    "Gobi",
                    "Baingan",
                ),
            ),
        )
        val hisab = repository.activeRozHisab.first()
        assertNotNull(hisab)
        assertEquals(0.0, hisab!!.totalSale, 0.01)
    }

    @Test
    fun `sale customer udhaar and roz hisab entities persist accurately`() = runTest {
        repository.ensureDefaultVegetablesSeeded()

        // 1. Add sale with partial payment -> total = 100 * 5 = 500, paid = 300, udhaar = 200
        repository.addSale(
            customerName = "Haji Akram",
            vegetableName = "Aloo",
            ratePerKg = 100.0,
            weightKg = 5.0,
            paidInput = 300.0,
        )

        val sales = repository.sales.first()
        assertEquals(1, sales.size)
        assertEquals(500.0, sales.first().totalAmount, 0.01)
        assertEquals(200.0, sales.first().udhaarAmount, 0.01)

        // Updated rate persisted for Aloo
        val updatedAloo = repository.vegetables.first().first { it.name == "Aloo" }
        assertEquals(100.0, updatedAloo.ratePerKg, 0.01)

        // Udhaar Khata shows Haji Akram with 200
        var udhaarList = repository.pendingUdhaarCustomers.first()
        assertEquals(1, udhaarList.size)
        assertEquals(200.0, udhaarList.first().pendingAmount, 0.01)

        // RozHisab persisted row reflects Sale, Cash, Udhaar, and Munafa
        var rozHisab = repository.activeRozHisab.first()!!
        assertEquals(500.0, rozHisab.totalSale, 0.01)
        assertEquals(300.0, rozHisab.totalCashMila, 0.01)
        assertEquals(200.0, rozHisab.totalUdhaar, 0.01)
        assertEquals(500.0, rozHisab.munafa, 0.01)

        // 2. Partial payment reduces udhaar
        repository.receiveUdhaarPayment(udhaarList.first().customerKey, 50.0)
        udhaarList = repository.pendingUdhaarCustomers.first()
        assertEquals(150.0, udhaarList.first().pendingAmount, 0.01)

        // 3. Remaining payment clears customer so they are hidden
        repository.receiveUdhaarPayment(udhaarList.first().customerKey, 150.0)
        udhaarList = repository.pendingUdhaarCustomers.first()
        assertTrue(udhaarList.isEmpty())

        // 4. Add Gaari + Mazdoori expenses and verify RozHisab updates Munafa (500 - 200 = 300)
        repository.addExpense("Gaari", "Subah kiraya", 120.0)
        repository.addExpense("Mazdoori", "", 80.0)
        assertEquals(2, repository.expenses.first().size)

        rozHisab = repository.activeRozHisab.first()!!
        assertEquals(120.0, rozHisab.gaariKharcha, 0.01)
        assertEquals(80.0, rozHisab.mazdooriKharcha, 0.01)
        assertEquals(200.0, rozHisab.totalKharcha, 0.01)
        assertEquals(300.0, rozHisab.munafa, 0.01)

        // 5. Din Khatam resets Sale, Expense, CustomerUdhaar, and RozHisab
        repository.endDay(clearUdhaarKhata = true)
        assertTrue(repository.sales.first().isEmpty())
        assertTrue(repository.expenses.first().isEmpty())
        assertTrue(repository.pendingUdhaarCustomers.first().isEmpty())

        rozHisab = repository.activeRozHisab.first()!!
        assertEquals(0.0, rozHisab.totalSale, 0.01)
        assertEquals(0.0, rozHisab.totalKharcha, 0.01)
        assertEquals(0.0, rozHisab.munafa, 0.01)
    }
}
