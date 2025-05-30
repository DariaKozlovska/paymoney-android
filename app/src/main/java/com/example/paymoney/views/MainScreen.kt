package com.example.paymoney.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paymoney.ui.theme.Blue
import com.example.paymoney.ui.theme.Gray

@Composable
fun MainScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Offers", Icons.Filled.ListAlt),
        TabItem("Account", Icons.Filled.AccountCircle),
        TabItem("Revolut", Icons.Filled.AccountBalanceWallet)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(34.dp),
                                tint = if (selectedTabIndex == index) {
                                    Blue
                                } else {
                                    Gray
                                }
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                color = if (selectedTabIndex == index) {
                                    Blue
                                } else {
                                    Gray
                                }
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color.Transparent,
                            unselectedIconColor = Color.Transparent
                        ),
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> OffersView()
                1 -> AccountView()
                2 -> RevolutTab()
            }
        }
    }
}


data class TabItem(val title: String, val icon: ImageVector)

@Composable
fun RevolutTab() {
    Text("Revolut content", modifier = Modifier.padding(16.dp))
}