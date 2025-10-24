package com.example.currencyconverter_ratewise.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.currencyconverter_ratewise.ui.screens.converter.ConverterScreen
import com.example.currencyconverter_ratewise.ui.screens.exchange.ExchangeScreen
import com.example.currencyconverter_ratewise.ui.screens.settings.SettingsViewModel
import com.example.currencyconverter_ratewise.ui.theme.CurrencyConverterRateWiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()
        installSplashScreen()

        val tabItems = listOf(
            TabItem(
                title = "Converter",
                selectedColor = Color(0xFF1B5E20),
                unselectedColor = Color(0xFF4CAF50)
            ),
            TabItem(
                title = "Exchange Rates",
                selectedColor = Color(0xFF0D47A1),
                unselectedColor = Color(0xFF2196F3)
            )
        )

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            CurrencyConverterRateWiseTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedTabIndex by remember {
                        mutableIntStateOf(0)
                    }
                    val pagerState = rememberPagerState {
                        tabItems.size
                    }
                    LaunchedEffect(selectedTabIndex) {
                        pagerState.animateScrollToPage(selectedTabIndex)
                    }
                    LaunchedEffect(pagerState.currentPage) {
                        selectedTabIndex = pagerState.currentPage
                    }
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                            tabItems.forEachIndexed { index, item ->
                                Tab(
                                    selected = index == selectedTabIndex,
                                    onClick = {
                                        selectedTabIndex = index
                                    },
                                    text = {
                                        Text(text = item.title)
                                    }
                                )
                            }
                        }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (it) {
                                0 -> ConverterScreen()
                                1 -> ExchangeScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(
    val title: String,
    val selectedColor: Color,
    val unselectedColor: Color
)