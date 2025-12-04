package com.example.currencyconverter_ratewise.ui.screens.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter_ratewise.data.model.Currency
import com.example.currencyconverter_ratewise.data.repository.CurrencyRepository
import com.example.currencyconverter_ratewise.domain.usecase.ConvertCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import java.util.*

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val convertCurrencyUseCase: ConvertCurrencyUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            try {
                val currencies = currencyRepository.getAvailableCurrencies()
                _uiState.update {
                    it.copy(
                        availableCurrencies = currencies,
                        fromCurrency = currencies.firstOrNull { it.code == "USD" } ?: currencies.first(),
                        toCurrency = currencies.firstOrNull { it.code == "NGN" } ?: currencies.last()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to load currencies: ${e.message}")
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount, error = null) }
        convertCurrency()
    }

    fun onFromCurrencyChange(currency: Currency) {
        _uiState.update { it.copy(fromCurrency = currency, error = null) }
        convertCurrency()
    }

    fun onToCurrencyChange(currency: Currency) {
        _uiState.update { it.copy(toCurrency = currency, error = null) }
        convertCurrency()
    }

    fun swapCurrencies() {
        _uiState.update {
            it.copy(
                fromCurrency = it.toCurrency,
                toCurrency = it.fromCurrency,
                error = null
            )
        }
        convertCurrency()
    }

    fun refresh() {
        convertCurrency()
    }

    private fun convertCurrency() {
        val state = _uiState.value
        if (state.amount.isBlank()) {
            _uiState.update { it.copy(result = "") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val amountDouble = state.amount.toDoubleOrNull() ?: 0.0
                val totalResult = currencyRepository.convertCurrency(
                    fromCurrency = state.fromCurrency.code,
                    toCurrency = state.toCurrency.code,
                    amount = amountDouble
                )

                val rate = if (amountDouble > 0) totalResult / amountDouble else 0.0
                val currentTime = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
                    .format(Date())

                _uiState.update {
                    it.copy(
                        result = String.format("%.2f", totalResult),
                        isLoading = false,
                        exchangeRate = rate,
                        lastUpdated = currentTime
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Conversion failed: ${e.message}"
                    )
                }
            }
        }
    }
}

data class ConverterUiState(
    val amount: String = "",
    val result: String = "",
    val fromCurrency: Currency = Currency("US Dollar", "USD"),
    val toCurrency: Currency = Currency("Euro", "EUR"),
    val availableCurrencies: List<Currency> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val exchangeRate: Double? = null,
    val lastUpdated: String? = null
)