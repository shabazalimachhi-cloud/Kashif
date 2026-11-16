package com.example.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomerUdhaar
import com.example.data.local.ExpenseEntity
import com.example.data.local.RozHisab
import com.example.data.local.Sale
import com.example.data.local.VegetableRateEntity
import com.example.data.repository.MandiRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyHisabSummary(
    val totalSale: Double = 0.0,
    val totalCashMila: Double = 0.0,
    val totalUdhaarToday: Double = 0.0,
    val totalKhataUdhaar: Double = 0.0,
    val totalKharcha: Double = 0.0,
    val gaariKharcha: Double = 0.0,
    val mazdooriKharcha: Double = 0.0,
    val otherKharcha: Double = 0.0,
    val munafa: Double = 0.0,
    val saleCount: Int = 0,
)

data class SaleFormState(
    val customerName: String = "",
    val selectedVegetable: String = "Aloo",
    val rateInput: String = "80",
    val kgInput: String = "",
    val paidInput: String = "",
    val errorMessage: String? = null,
) {
    val rateValue: Double get() = rateInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val kgValue: Double get() = kgInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val totalAmount: Double get() = MandiRepository.roundCurrency(rateValue * kgValue)
    val paidValue: Double get() = paidInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val appliedPaid: Double get() = paidValue.coerceAtMost(totalAmount)
    val udhaarPreview: Double get() = MandiRepository.roundCurrency((totalAmount - appliedPaid).coerceAtLeast(0.0))
    val changePreview: Double get() = MandiRepository.roundCurrency((paidValue - totalAmount).coerceAtLeast(0.0))
}

class MandiViewModel(private val repository: MandiRepository) : ViewModel() {

    private val _saleForm = MutableStateFlow(SaleFormState())
    val saleForm: StateFlow<SaleFormState> = _saleForm.asStateFlow()

    private val _udhaarSearch = MutableStateFlow("")
    val udhaarSearch: StateFlow<String> = _udhaarSearch.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    val vegetables: StateFlow<List<VegetableRateEntity>> = repository.vegetables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.sales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCustomerNames: StateFlow<List<String>> = repository.recentCustomerNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingUdhaarCustomers: StateFlow<List<CustomerUdhaar>> =
        combine(repository.pendingUdhaarCustomers, _udhaarSearch) { list, query ->
            val clean = query.trim().lowercase()
            if (clean.isEmpty()) list
            else list.filter { it.displayName.lowercase().contains(clean) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyHisab: StateFlow<DailyHisabSummary> =
        combine(
            repository.activeRozHisab,
            repository.pendingUdhaarCustomers,
        ) { hisab, udhaarList ->
            val persisted = hisab ?: RozHisab(dateKey = MandiRepository.currentDateKey())
            val totalKhataUdhaar = MandiRepository.roundCurrency(udhaarList.sumOf { it.pendingAmount })
            DailyHisabSummary(
                totalSale = persisted.totalSale,
                totalCashMila = persisted.totalCashMila,
                totalUdhaarToday = persisted.totalUdhaar,
                totalKhataUdhaar = totalKhataUdhaar,
                totalKharcha = persisted.totalKharcha,
                gaariKharcha = persisted.gaariKharcha,
                mazdooriKharcha = persisted.mazdooriKharcha,
                otherKharcha = persisted.otherKharcha,
                munafa = persisted.munafa,
                saleCount = persisted.saleCount,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyHisabSummary())

    init {
        viewModelScope.launch {
            repository.ensureDefaultVegetablesSeeded()
        }
        viewModelScope.launch {
            repository.vegetables.collect { list ->
                if (list.isNotEmpty()) {
                    val current = _saleForm.value
                    val matched = list.find { it.name == current.selectedVegetable } ?: list.first()
                    if (current.kgInput.isEmpty() && current.paidInput.isEmpty() && current.selectedVegetable == matched.name) {
                        val formattedRate = formatCleanNumberInput(matched.ratePerKg)
                        if (current.rateInput != formattedRate) {
                            _saleForm.value = current.copy(rateInput = formattedRate)
                        }
                    }
                }
            }
        }
    }

    fun onCustomerNameChanged(value: String) {
        _saleForm.value = _saleForm.value.copy(customerName = value, errorMessage = null)
    }

    fun onVegetableSelected(veg: VegetableRateEntity) {
        _saleForm.value = _saleForm.value.copy(
            selectedVegetable = veg.name,
            rateInput = formatCleanNumberInput(veg.ratePerKg),
            errorMessage = null,
        )
    }

    fun onRateChanged(value: String) {
        if (isValidDecimalInput(value)) {
            _saleForm.value = _saleForm.value.copy(rateInput = value, errorMessage = null)
        }
    }

    fun onKgChanged(value: String) {
        if (isValidDecimalInput(value)) {
            _saleForm.value = _saleForm.value.copy(kgInput = value, errorMessage = null)
        }
    }

    fun addQuickKg(deltaKg: Double) {
        val current = _saleForm.value.kgValue
        val updated = current + deltaKg
        _saleForm.value = _saleForm.value.copy(
            kgInput = formatCleanNumberInput(updated),
            errorMessage = null,
        )
    }

    fun onPaidChanged(value: String) {
        if (isValidDecimalInput(value)) {
            _saleForm.value = _saleForm.value.copy(paidInput = value, errorMessage = null)
        }
    }

    fun fillFullCash() {
        val total = _saleForm.value.totalAmount
        if (total > 0.0) {
            _saleForm.value = _saleForm.value.copy(
                paidInput = formatCleanNumberInput(total),
                errorMessage = null,
            )
        }
    }

    fun submitSale() {
        val form = _saleForm.value
        val rate = form.rateInput.toDoubleOrNull()
        val kg = form.kgInput.toDoubleOrNull()
        val paid = if (form.paidInput.isBlank()) 0.0 else form.paidInput.toDoubleOrNull()

        if (rate == null || rate <= 0.0) {
            _saleForm.value = form.copy(errorMessage = "Sahi rate darj karein (0 se zyada).")
            return
        }
        if (kg == null || kg <= 0.0) {
            _saleForm.value = form.copy(errorMessage = "Sabzi ka wazan (KG) likhein.")
            return
        }
        if (paid == null || paid < 0.0) {
            _saleForm.value = form.copy(errorMessage = "Sahi raqam likhein.")
            return
        }

        val total = MandiRepository.roundCurrency(rate * kg)
        val willBeUdhaar = total - paid.coerceAtMost(total) > 0.01
        if (willBeUdhaar && form.customerName.trim().isEmpty()) {
            _saleForm.value = form.copy(
                errorMessage = "Udhaar sale ke liye customer ka naam likhna zaroori hai.",
            )
            return
        }

        val savedCustomer = form.customerName.trim().ifEmpty { "Naqad Customer" }
        val vegName = form.selectedVegetable
        viewModelScope.launch {
            repository.addSale(
                customerName = savedCustomer,
                vegetableName = vegName,
                ratePerKg = rate,
                weightKg = kg,
                paidInput = paid,
            )
            _saleForm.value = _saleForm.value.copy(
                customerName = "",
                kgInput = "",
                paidInput = "",
                rateInput = formatCleanNumberInput(rate),
                errorMessage = null,
            )
            _messages.emit("$savedCustomer ki $vegName sale add ho gayi!")
        }
    }

    fun deleteSale(sale: Sale) {
        viewModelScope.launch {
            repository.deleteSale(sale.id)
            _messages.emit("Sale entry delete ho gayi.")
        }
    }

    fun onUdhaarSearchChanged(query: String) {
        _udhaarSearch.value = query
    }

    fun receiveUdhaarPayment(customer: CustomerUdhaar, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.receiveUdhaarPayment(customer.customerKey, amount)
            val remaining = (customer.pendingAmount - amount).coerceAtLeast(0.0)
            if (remaining <= 0.01) {
                _messages.emit("${customer.displayName} ka hisab clear ho gaya!")
            } else {
                _messages.emit("${customer.displayName} se wasooli darj ho gayi.")
            }
        }
    }

    fun addExpense(category: String, note: String, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpense(category, note, amount)
            _messages.emit("$category kharcha add ho gaya.")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense.id)
            _messages.emit("Kharcha entry delete ho gayi.")
        }
    }

    fun endDay(clearUdhaarKhata: Boolean) {
        viewModelScope.launch {
            repository.endDay(clearUdhaarKhata)
            _messages.emit(
                if (clearUdhaarKhata) "Din khatam! Saari sales, kharcha aur udhaar clear ho gaya."
                else "Din khatam! Aaj ki sales aur kharcha clear ho gaya."
            )
        }
    }

    private fun isValidDecimalInput(input: String): Boolean {
        if (input.isEmpty()) return true
        return input.matches(Regex("^\\d*\\.?\\d{0,2}$"))
    }

    private fun formatCleanNumberInput(value: Double): String {
        val rounded = MandiRepository.roundCurrency(value)
        return if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    class Factory(private val repository: MandiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MandiViewModel(repository) as T
        }
    }
}
