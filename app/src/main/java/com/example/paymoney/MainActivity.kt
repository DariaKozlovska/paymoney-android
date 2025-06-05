package com.example.paymoney

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
import com.example.paymoney.views.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PaymoneyTheme {
                val ethereum = remember { AppModule.provideEthereum(this@MainActivity) }
                val viewModel: MainViewModel = viewModel { MainViewModel(ethereum) }

                MainScreen(viewModel = viewModel)
            }
        }
    }
}

