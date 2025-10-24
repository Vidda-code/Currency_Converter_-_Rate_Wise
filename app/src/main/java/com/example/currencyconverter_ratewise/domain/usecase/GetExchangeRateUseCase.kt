package com.example.currencyconverter_ratewise.domain.usecase

import com.example.currencyconverter_ratewise.data.repository.CurrencyRepositoryImpl
import javax.inject.Inject

class GetExchangeRatesUseCase @Inject constructor(
    private val repository: CurrencyRepositoryImpl
) {
    suspend operator fun invoke(baseCurrency: String): Map<String, Double> {
        return repository.getExchangeRates(baseCurrency)
    }
}