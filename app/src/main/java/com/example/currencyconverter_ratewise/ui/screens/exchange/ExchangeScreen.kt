package com.example.currencyconverter_ratewise.ui.screens.exchange

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import com.example.currencyconverter_ratewise.data.model.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    modifier: Modifier = Modifier,
    viewModel: ExchangeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBaseCurrencySheet by remember { mutableStateOf(false) }
    var showAddCurrenciesSheet by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Base Currency Selector Card
            BaseCurrencyCard(
                currency = uiState.baseCurrency,
                onCardClick = { showBaseCurrencySheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add Currencies Button (only show if there are hidden currencies)
            if (uiState.hiddenCurrencies.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showAddCurrenciesSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Currencies",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Currencies")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Exchange Rates List
            if (uiState.isLoading && uiState.exchangeRates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null && uiState.exchangeRates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter to show only currencies in our available list and not hidden
                    val filteredRates = uiState.exchangeRates.filter { (code, _) ->
                        uiState.availableCurrencies.any { it.code == code } &&
                                !uiState.hiddenCurrencies.contains(code)
                    }

                    items(
                        items = filteredRates.toList(),
                        key = { it.first }
                    ) { (currencyCode, rate) ->
                        val currency = uiState.availableCurrencies.find { it.code == currencyCode }
                        if (currency != null) {
                            SwipeToDeleteItem(
                                currency = currency,
                                rate = rate,
                                baseCurrency = uiState.baseCurrency.code,
                                onDelete = { viewModel.hideCurrency(currencyCode) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Info Section
            ExchangeBottomInfoSection(
                lastUpdated = getCurrentTimestamp(),
                onRefresh = { viewModel.refresh() },
                onSettingsClick = { showSettings = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Loading overlay
        if (uiState.isLoading && uiState.exchangeRates.isNotEmpty()) {
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

    // Base Currency Bottom Sheet
    if (showBaseCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showBaseCurrencySheet = false },
            sheetState = sheetState
        ) {
            BaseCurrencySelectionSheet(
                title = "Select Base Currency",
                currencies = uiState.availableCurrencies,
                onCurrencySelected = { currency ->
                    viewModel.onBaseCurrencyChange(currency)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBaseCurrencySheet = false
                    }
                }
            )
        }
    }

    // Add Currencies Bottom Sheet
    if (showAddCurrenciesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCurrenciesSheet = false },
            sheetState = sheetState
        ) {
            AddCurrenciesSheet(
                title = "Add Currencies",
                hiddenCurrencies = uiState.hiddenCurrencies,
                availableCurrencies = uiState.availableCurrencies,
                onCurrencyAdded = { currencyCode ->
                    viewModel.showCurrency(currencyCode)
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showAddCurrenciesSheet = false
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
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteItem(
    currency: Currency,
    rate: Double,
    baseCurrency: String,
    onDelete: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    isVisible = false
                    true
                }
                else -> false
            }
        }
    )

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = shrinkVertically() + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE53935), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            ExchangeRateItem(
                currency = currency,
                rate = rate,
                baseCurrency = baseCurrency
            )
        }
    }
}

@Composable
fun BaseCurrencyCard(
    currency: Currency,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCurrencyFlag(currency.code),
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = currency.code,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currency.name,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = "▼",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ExchangeRateItem(
    currency: Currency,
    rate: Double,
    baseCurrency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCurrencyFlag(currency.code),
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = currency.code,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currency.name,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.4f", rate),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Text(
                    text = "1 $baseCurrency",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AddCurrenciesSheet(
    title: String,
    hiddenCurrencies: Set<String>,
    availableCurrencies: List<Currency>,
    onCurrencyAdded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter to show only hidden currencies
        val hiddenCurrenciesList = availableCurrencies.filter {
            hiddenCurrencies.contains(it.code)
        }

        if (hiddenCurrenciesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hidden currencies",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hiddenCurrenciesList) { currency ->
                    AddCurrencyItem(
                        currency = currency,
                        onClick = { onCurrencyAdded(currency.code) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AddCurrencyItem(
    currency: Currency,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ExchangeBottomInfoSection(
    lastUpdated: String,
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
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.Gray
            )
        }

        Text(
            text = "Last updated: $lastUpdated",
            fontSize = 12.sp,
            color = Color(0xFF2196F3),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

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
fun BaseCurrencySelectionSheet(
    title: String,
    currencies: List<Currency>,
    onCurrencySelected: (Currency) -> Unit
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currencies) { currency ->
                BaseCurrencyItem(
                    currency = currency,
                    onClick = { onCurrencySelected(currency) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BaseCurrencyItem(
    currency: Currency,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
fun SettingsSheet(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dark Mode", fontSize = 16.sp)
                Switch(checked = false, onCheckedChange = { /* TODO */ })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Submit Feedback", fontSize = 16.sp)
                Text(text = ">", fontSize = 20.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "About", fontSize = 16.sp)
                Text(text = ">", fontSize = 20.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

fun getCurrentTimestamp(): String {
    return SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(Date())
}