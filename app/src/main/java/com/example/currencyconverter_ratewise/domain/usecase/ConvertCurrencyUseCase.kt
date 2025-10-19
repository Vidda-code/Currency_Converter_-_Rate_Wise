package com.example.currencyconverter_ratewise.domain.usecase

import com.example.currencyconverter_ratewise.data.repository.CurrencyRepository

class ConvertCurrencyUseCase(
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(
        fromCurrency: String,
        toCurrency: String,
        amount: String
    ): String {
        if (fromCurrency.isBlank()) return ""
        if (toCurrency.isBlank()) return ""
        if (amount.isBlank()) return ""
        if (fromCurrency == toCurrency) return amount

        val amountDouble = amount.toDoubleOrNull() ?: return ""

        val result = currencyRepository.convertCurrency(
            fromCurrency,
            toCurrency,
            amountDouble
        )
        return result.toString()
    }

}