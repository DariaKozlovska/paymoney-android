package com.example.paymoney.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountView(modifier: Modifier = Modifier) {
    Text(
        text = "Account View Content",
        modifier = modifier.padding(16.dp)
    )
}