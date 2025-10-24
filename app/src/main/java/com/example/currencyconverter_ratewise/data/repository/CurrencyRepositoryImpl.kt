package com.example.currencyconverter_ratewise.data.repository

import com.example.currencyconverter_ratewise.data.model.Currency
import com.example.currencyconverter_ratewise.data.remote.ApiService
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CurrencyRepository {

    private val apiKey = "a76ffb6dc5c486d789b1dba4"

    override suspend fun convertCurrency(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): Double {
        return try {
            val response = apiService.convertCurrency(
                apiKey = apiKey,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                amount = amount
            )
            response.conversion_result
        } catch (e: Exception) {
            throw Exception("Failed to convert currency: ${e.message}")
        }
    }

    override suspend fun getAvailableCurrencies(): List<Currency> {
        // This returns a hardcoded list of popular currencies
        // You can expand this or fetch from API if needed
        return listOf(
            Currency("US Dollar", "USD"),
            Currency("Euro", "EUR"),
            Currency("British Pound", "GBP"),
            Currency("Japanese Yen", "JPY"),
            Currency("Canadian Dollar", "CAD"),
            Currency("Australian Dollar", "AUD"),
            Currency("Swiss Franc", "CHF"),
            Currency("Chinese Yuan", "CNY"),
            Currency("Indian Rupee", "INR"),
            Currency("Nigerian Naira", "NGN"),
            Currency("South African Rand", "ZAR"),
            Currency("Mexican Peso", "MXN"),
            Currency("Brazilian Real", "BRL"),
            Currency("South Korean Won", "KRW"),
            Currency("Singapore Dollar", "SGD"),
            Currency("Hong Kong Dollar", "HKD"),
            Currency("Swedish Krona", "SEK"),
            Currency("Norwegian Krone", "NOK"),
            Currency("New Zealand Dollar", "NZD"),
            Currency("Turkish Lira", "TRY")
        ).sortedBy { it.name }
    }

    suspend fun getExchangeRates(baseCurrency: String): Map<String, Double> {
        return try {
            val response = apiService.getExchangeRates(apiKey, baseCurrency)
            response.conversion_rates
        } catch (e: Exception) {
            throw Exception("Failed to fetch exchange rates: ${e.message}")
        }
    }
}