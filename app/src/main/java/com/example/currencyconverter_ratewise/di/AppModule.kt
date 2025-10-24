package com.example.currencyconverter_ratewise.di

import android.content.Context
import com.example.currencyconverter_ratewise.data.preferences.ThemePreferences
import com.example.currencyconverter_ratewise.data.remote.ApiService
import com.example.currencyconverter_ratewise.data.repository.CurrencyRepository
import com.example.currencyconverter_ratewise.data.repository.CurrencyRepositoryImpl
import com.example.currencyconverter_ratewise.domain.usecase.ConvertCurrencyUseCase
import com.example.currencyconverter_ratewise.domain.usecase.GetExchangeRatesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCurrencyRepository(apiService: ApiService): CurrencyRepository {
        return CurrencyRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideCurrencyRepositoryImpl(apiService: ApiService): CurrencyRepositoryImpl {
        return CurrencyRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideConvertCurrencyUseCase(repository: CurrencyRepository): ConvertCurrencyUseCase {
        return ConvertCurrencyUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetExchangeRatesUseCase(repository: CurrencyRepositoryImpl): GetExchangeRatesUseCase {
        return GetExchangeRatesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }
}