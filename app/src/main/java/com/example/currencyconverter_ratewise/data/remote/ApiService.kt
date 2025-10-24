package com.example.currencyconverter_ratewise.data.remote

import com.example.currencyconverter_ratewise.data.remote.dto.ExchangeRateResponse
import com.example.currencyconverter_ratewise.data.remote.dto.ExchangeRatesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("v6/{apiKey}/pair/{from}/{to}/{amount}")
    suspend fun convertCurrency(
        @Path("apiKey") apiKey: String,
        @Path("from") fromCurrency: String,
        @Path("to") toCurrency: String,
        @Path("amount") amount: Double
    ): ExchangeRateResponse

    @GET("v6/{apiKey}/latest/{base}")
    suspend fun getExchangeRates(
        @Path("apiKey") apiKey: String,
        @Path("base") baseCurrency: String
    ): ExchangeRatesResponse
}