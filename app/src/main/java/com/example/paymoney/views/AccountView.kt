package com.example.paymoney.views

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paymoney.ui.theme.Blue
import com.example.paymoney.EventSink
import com.example.paymoney.MainUiState

@Composable
fun AccountView(
    uiState: MainUiState,
    onEvent: (EventSink) -> Unit
) {
    Column (
        modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
    ){
        Text("Your account",
            fontSize = 38.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        InfoRow("Network", "Ethereum") // You could later make this dynamic
        InfoRow("Status", if (uiState.isConnected && uiState.isSigned) "Signed" else if (uiState.isConnected) "Connected" else "")
        InfoRow("Chain ID", if (uiState.chainId.isNotEmpty()) "${uiState.chainId}" else "") // Add real Chain ID if available
        InfoRow(
            "Account",
            uiState.address.takeIf { it.length >= 10 }?.let {
                "${it.take(4)}...${it.takeLast(4)}"
            } ?: ""
        )
        InfoRow("Balance", uiState.balance)


        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(
                onClick = {
                    if (!uiState.isConnected) {
                        onEvent(EventSink.Connect)
                    } else {
                        onEvent(EventSink.SignMessage)
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Connect and Sign Metamask", textAlign = TextAlign.Center, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onEvent(EventSink.Disconnect)},
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Disconnect", textAlign = TextAlign.Center, fontSize = 16.sp)
            }
        }

    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, color = Color.DarkGray)
    }
}
