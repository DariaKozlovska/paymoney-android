package com.example.paymoney.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paymoney.EventSink
import com.example.paymoney.MainUiState
import com.example.paymoney.ui.theme.Blue
import com.example.paymoney.ui.theme.Gray
import com.example.paymoney.utils.CustomDropdown

@Composable
fun OffersView(
    uiState: MainUiState,
    onEvent: (EventSink) -> Unit
) {
    val blockchainOptions = listOf("Ethereum", "Polygon")
    val allowedCurrenciesForBlockchain = remember {
        mapOf(
            "Polygon" to listOf("USDC"),
            "Ethereum" to listOf("USDC", "USDT")
        )
    }

    var selectedBlockchain by remember { mutableStateOf("Ethereum") }
    var selectedCurrency by remember { mutableStateOf("USDC") }
    val availableCurrencies by remember(selectedBlockchain) {
        derivedStateOf { allowedCurrenciesForBlockchain[selectedBlockchain] ?: emptyList() }
    }

    LaunchedEffect(selectedBlockchain) {
        if (!availableCurrencies.contains(selectedCurrency)) {
            selectedCurrency = availableCurrencies.firstOrNull() ?: "USDC"
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Offers",
            fontSize = 38.sp,
            fontWeight = FontWeight.W700,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            // Blockchain Dropdown
            CustomDropdown(
                modifier = Modifier.weight(1f),
                options = blockchainOptions,
                selectedOption = selectedBlockchain,
                onOptionSelected = { selectedBlockchain = it }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Currency Dropdown
            CustomDropdown(
                modifier = Modifier.weight(1f),
                options = availableCurrencies,
                selectedOption = selectedCurrency,
                onOptionSelected = { selectedCurrency = it }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No offers available",
                color = Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(8.dp),
            enabled = uiState.isConnected
        ) {
            Text(text = "Create Offer", color = Color.White, fontSize = 18.sp)
        }
    }
}