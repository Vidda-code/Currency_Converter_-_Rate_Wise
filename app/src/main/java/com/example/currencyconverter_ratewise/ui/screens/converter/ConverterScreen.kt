package com.example.currencyconverter_ratewise.ui.screens.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.currencyconverter_ratewise.ui.screens.about.AboutSheet
import com.example.currencyconverter_ratewise.ui.screens.feedback.FeedbackSheet
import com.example.currencyconverter_ratewise.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    modifier: Modifier = Modifier,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showFromCurrencySheet by remember { mutableStateOf(false) }
    var showToCurrencySheet by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // From Currency Card
            CurrencyCard(
                currency = uiState.fromCurrency,
                amount = uiState.amount,
                onCardClick = { showFromCurrencySheet = true },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Swap Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { viewModel.swapCurrencies() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, shape = RoundedCornerShape(28.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // To Currency Card
            CurrencyCard(
                currency = uiState.toCurrency,
                amount = uiState.result,
                onCardClick = { showToCurrencySheet = true },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Number Pad
            CustomNumberPad(
                onNumberClick = { number ->
                    viewModel.onAmountChange(uiState.amount + number)
                },
                onDecimalClick = {
                    if (!uiState.amount.contains(".")) {
                        viewModel.onAmountChange(uiState.amount + ".")
                    }
                },
                onDeleteClick = {
                    if (uiState.amount.isNotEmpty()) {
                        viewModel.onAmountChange(uiState.amount.dropLast(1))
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Info Section
            BottomInfoSection(
                fromCurrency = uiState.fromCurrency.code,
                toCurrency = uiState.toCurrency.code,
                exchangeRate = uiState.exchangeRate,
                lastUpdated = uiState.lastUpdated,
                onRefresh = { viewModel.refresh() },
                onSettingsClick = { showSettings = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // Error Snackbar
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Show snackbar or toast here
        }
    }

    // From Currency Bottom Sheet
    if (showFromCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showFromCurrencySheet = false },
            sheetState = sheetState
        ) {
            CurrencySelectionSheet(
                title = "Select Currency",
                currencies = uiState.availableCurrencies,
                onCurrencySelected = { currency ->
                    viewModel.onFromCurrencyChange(currency)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showFromCurrencySheet = false
                    }
                }
            )
        }
    }

    // To Currency Bottom Sheet
    if (showToCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showToCurrencySheet = false },
            sheetState = sheetState
        ) {
            CurrencySelectionSheet(
                title = "Select Currency",
                currencies = uiState.availableCurrencies,
                onCurrencySelected = { currency ->
                    viewModel.onToCurrencyChange(currency)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showToCurrencySheet = false
                    }
                }
            )
        }
    }
    // Settings Bottom Sheet
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState
        ) {
            SettingsSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSettings = false
                    }
                },
                onFeedbackClick = {
                    showSettings = false
                    showFeedback = true
                },
                onAboutClick = {
                    showSettings = false
                    showAbout = true
                }
            )
        }
    }

// Feedback Bottom Sheet
    if (showFeedback) {
        ModalBottomSheet(
            onDismissRequest = { showFeedback = false },
            sheetState = sheetState
        ) {
            FeedbackSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showFeedback = false
                    }
                }
            )
        }
    }

// About Bottom Sheet
    if (showAbout) {
        ModalBottomSheet(
            onDismissRequest = { showAbout = false },
            sheetState = sheetState
        ) {
            AboutSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showAbout = false
                    }
                }
            )
        }
    }
}

@Composable
fun CurrencyCard(
    currency: com.example.currencyconverter_ratewise.data.model.Currency,
    amount: String,
    onCardClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Currency Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onCardClick)
            ) {
                Text(
                    text = getCurrencyFlag(currency.code),
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currency.code,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " ▼",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // Amount Display
            if (isLoading && amount.isNotBlank()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (amount.isBlank()) getCurrencySymbol(currency.code) + "0"
                    else getCurrencySymbol(currency.code) + amount,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
            }
        }
    }
}

@Composable
fun BottomInfoSection(
    fromCurrency: String,
    toCurrency: String,
    exchangeRate: Double?,
    lastUpdated: String?,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Refresh Button
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.Gray
            )
        }

        // Exchange Rate Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (lastUpdated != null) {
                Text(
                    text = lastUpdated,
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
            }
            if (exchangeRate != null) {
                Text(
                    text = "1 $fromCurrency = ${String.format("%.6f", exchangeRate)} $toCurrency",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Settings Button
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun CustomNumberPad(
    onNumberClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val buttons = listOf(
        listOf("7", "8", "9"),
        listOf("4", "5", "6"),
        listOf("1", "2", "3"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { button ->
                    NumberButton(
                        text = button,
                        onClick = {
                            when (button) {
                                "." -> onDecimalClick()
                                "⌫" -> onDeleteClick()
                                else -> onNumberClick(button)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NumberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE0E0E0),
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun CurrencySelectionSheet(
    title: String,
    currencies: List<com.example.currencyconverter_ratewise.data.model.Currency>,
    onCurrencySelected: (com.example.currencyconverter_ratewise.data.model.Currency) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        currencies.forEach { currency ->
            CurrencyItem(
                currency = currency,
                onClick = { onCurrencySelected(currency) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CurrencyItem(
    currency: com.example.currencyconverter_ratewise.data.model.Currency,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = getCurrencyFlag(currency.code), fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${currency.name} (${currency.code})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(text = "+", fontSize = 24.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    onFeedbackClick: () -> Unit,
    onAboutClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Dark Mode Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark Mode",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { settingsViewModel.toggleDarkMode(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Feedback
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFeedbackClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submit Feedback",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ">",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // About
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAboutClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "About",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ">",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Version
        Text(
            text = "Rate Wise\nv0.0.1",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Helper functions
fun getCurrencySymbol(code: String): String {
    return when (code) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "NGN" -> "₦"
        "INR" -> "₹"
        else -> "$"
    }
}

fun getCurrencyFlag(code: String): String {
    return when (code) {
        "USD" -> "🇺🇸"
        "EUR" -> "🇪🇺"
        "GBP" -> "🇬🇧"
        "JPY" -> "🇯🇵"
        "CAD" -> "🇨🇦"
        "AUD" -> "🇦🇺"
        "CHF" -> "🇨🇭"
        "CNY" -> "🇨🇳"
        "INR" -> "🇮🇳"
        "NGN" -> "🇳🇬"
        "ZAR" -> "🇿🇦"
        "MXN" -> "🇲🇽"
        "BRL" -> "🇧🇷"
        "KRW" -> "🇰🇷"
        "SGD" -> "🇸🇬"
        "HKD" -> "🇭🇰"
        "SEK" -> "🇸🇪"
        "NOK" -> "🇳🇴"
        "NZD" -> "🇳🇿"
        "TRY" -> "🇹🇷"
        else -> "🌍"
    }
}