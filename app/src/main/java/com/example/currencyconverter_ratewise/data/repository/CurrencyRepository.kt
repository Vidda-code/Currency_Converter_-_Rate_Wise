package com.example.currencyconverter_ratewise.data.repository

import com.example.currencyconverter_ratewise.data.model.Currency

interface CurrencyRepository {
    suspend fun convertCurrency(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): Double

    suspend fun getAvailableCurrencies(): List<Currency>
}