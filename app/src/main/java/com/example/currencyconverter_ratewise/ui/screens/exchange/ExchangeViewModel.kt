package com.example.currencyconverter_ratewise.ui.screens.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter_ratewise.data.model.Currency
import com.example.currencyconverter_ratewise.data.repository.CurrencyRepository
import com.example.currencyconverter_ratewise.domain.usecase.GetExchangeRatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeUiState())
    val uiState: StateFlow<ExchangeUiState> = _uiState.asStateFlow()

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
                        baseCurrency = currencies.firstOrNull { it.code == "USD" } ?: currencies.first()
                    )
                }
                loadExchangeRates()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to load currencies: ${e.message}")
                }
            }
        }
    }

    fun onBaseCurrencyChange(currency: Currency) {
        _uiState.update { it.copy(baseCurrency = currency, error = null) }
        loadExchangeRates()
    }

    private fun loadExchangeRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val rates = getExchangeRatesUseCase(_uiState.value.baseCurrency.code)
                _uiState.update {
                    it.copy(
                        exchangeRates = rates,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load exchange rates: ${e.message}"
                    )
                }
            }
        }
    }

    fun hideCurrency(currencyCode: String) {
        _uiState.update {
            it.copy(hiddenCurrencies = it.hiddenCurrencies + currencyCode)
        }
    }

    fun showCurrency(currencyCode: String) {
        _uiState.update {
            it.copy(hiddenCurrencies = it.hiddenCurrencies - currencyCode)
        }
    }

    fun refresh() {
        loadExchangeRates()
    }
}

data class ExchangeUiState(
    val baseCurrency: Currency = Currency("US Dollar", "USD"),
    val availableCurrencies: List<Currency> = emptyList(),
    val exchangeRates: Map<String, Double> = emptyMap(),
    val hiddenCurrencies: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)