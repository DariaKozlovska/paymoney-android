package com.example.paymoney

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paymoney.metamask.AppModule
import com.example.paymoney.ui.theme.Blue
import com.example.paymoney.ui.theme.PaymoneyTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaymoneyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val ethereum = remember { AppModule.provideEthereum(this@MainActivity) }
                    val viewModel: MainViewModel = viewModel { MainViewModel(ethereum) }
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called with intent: $intent")
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()

    LaunchedEffect(uiEvent) {
        uiEvent?.let { event ->
            when (event) {
                is UIEvent.ShowMessage -> {
                    println("Message: ${event.message}")
                }
            }
            viewModel.clearEvent()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (uiState.isConnected) "Connected" else "Not Connected",
            fontSize = 16.sp,
            color = if (uiState.isConnected) Color.Green else Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.address.isNotEmpty()) {
            Text(
                text = "Address: ${uiState.address}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (uiState.isConnected) {
                    viewModel.eventSink(EventSink.Disconnect)
                } else {
                    viewModel.eventSink(EventSink.Connect)
                }
            },
            enabled = !uiState.isConnecting,
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text(
                if (uiState.isConnecting) "Connecting..."
                else if (uiState.isConnected) "Disconnect Wallet"
                else "Connect Wallet"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.eventSink(EventSink.SignMessage) },
            enabled = uiState.isConnected,
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text("Sign Message")
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Get Balance Button
        Button(
            onClick = { viewModel.eventSink(EventSink.GetBalance) },
            enabled = uiState.isConnected,
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text("Get Balance")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Display
        Text(
            text = "Balance: ${uiState.balance}",
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}
