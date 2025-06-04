package com.example.paymoney.views

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
import io.metamask.androidsdk.Ethereum

@Composable
fun AccountView(
    onViewOffers: () -> Unit = {},
    onViewTransactions: () -> Unit = {},
    onCreateAffiliation: () -> Unit = {},
    onConnectWallet: () -> Unit = {},
    onDisconnect: () -> Unit = {}
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
        AccountSection(
            title = "Network",
            items = listOf()
        )
        AccountSection(
            title = "Status",
            items = listOf()
        )
        AccountSection(
            title = "Chain ID",
            items = listOf()
        )
        AccountSection(
            title = "Account",
            items = listOf()
        )
        AccountSection(
            title = "Balance",
            items = listOf()
        )
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(
                onClick = {},
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Connect & Sign Metamask", textAlign = TextAlign.Center, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { /* TODO */},
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
private fun AccountSection(
    title: String,
    items: List<AccountItem>
) {
    Row(
        modifier = Modifier
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = title,
        )
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.text
                )
                if (item.withCheckmark) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color.Green,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

private data class AccountItem(
    val text: String,
    val withCheckmark: Boolean
)