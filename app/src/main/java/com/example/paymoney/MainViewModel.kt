package com.example.paymoney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.RequestError
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import android.util.Log

sealed class UIEvent {
    data class ShowMessage(val message: String) : UIEvent()
}

sealed class EventSink {
    object Connect : EventSink()
    object GetBalance : EventSink()
    object Disconnect : EventSink()
}

data class MainUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val balance: String = "0 ETH",
    val address: String = ""
)

class MainViewModel(
    private val ethereum: Ethereum
) : ViewModel() {

    private val _uiEvent = MutableStateFlow<UIEvent?>(null)
    val uiEvent: StateFlow<UIEvent?> = _uiEvent.asStateFlow()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UIEvent.ShowMessage(message))
        }
    }

    private fun updateState(update: (MainUiState) -> MainUiState) {
        _uiState.value = update(_uiState.value)
    }

    fun eventSink(event: EventSink) {
        viewModelScope.launch {
            when (event) {
                EventSink.Connect -> connectWallet()
                EventSink.GetBalance -> getBalance()
                EventSink.Disconnect -> disconnectWallet()
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

//    private fun connectWallet() {
//        updateState { it.copy(isConnecting = true) }
//
//        ethereum.connect { result ->
//            viewModelScope.launch {
//                if (result is Result.Error) {
//                    updateState { it.copy(isConnecting = false) }
//                    showMessage("Connection failed: ${result.error.message}")
//                } else if (result is Result.Success) {
//                    val accounts = result as? List<*>
//                    if (accounts?.isNotEmpty() == true) {
//                        updateState {
//                            it.copy(
//                                isConnecting = false,
//                                isConnected = true,
//                                address = accounts.first().toString()
//                            )
//                        }
//                        showMessage("Connected successfully!")
//                    } else {
//                        updateState { it.copy(isConnecting = false) }
//                        showMessage("Connection failed: No accounts returned")
//                    }
//                }
//            }
//        }
//    }

    private fun connectWallet() {
        updateState { it.copy(isConnecting = true) }

        ethereum.connect { result ->
            viewModelScope.launch {
                when (result) {
                    is Result.Error -> {
                        updateState { it.copy(isConnecting = false) }
                        showMessage("Łączenie nie powiodło się : ${result.error.message}")
                    }
                    is Result.Success -> {
                        Log.d(TAG, "Łączenie wynik: ${result}")
                        Log.d(TAG, "Result class: ${result::class.java}")
                        Log.d(TAG, "Result class: ${result::class.simpleName}")
                        val item = result as Result.Success.Item
                        val accounts = item.value
                        Log.d(TAG, "Unwrapped data: $accounts")
//                        val accounts = result as? List<*>
                        if (accounts?.isNotEmpty() == true) {
                            updateState {
                                it.copy(
                                    isConnecting = false,
                                    isConnected = true,
                                    address = accounts.toString()
                                )
                            }
                            showMessage("Połączono")
                        } else {
                            updateState { it.copy(isConnecting = false) }
                            showMessage("Łączenie nie powiodło się: brak konta")
                        }
                    }
                }
            }
        }

    }

    private fun getBalance() {
        if (!uiState.value.isConnected || uiState.value.address.isEmpty()) {
            showMessage("Please connect wallet first")
            return
        }

        val request = EthereumRequest(
            method = "eth_getBalance",
            params = listOf(uiState.value.address, "latest")
        )

        ethereum.sendRequest(request) { result ->
            viewModelScope.launch {
                if (result is Result.Error) {
                    showMessage("Failed to get balance: ${result.error.message}")
                } else if (result is Result.Success) {
                    try {
                        // Changed 'result.data' to 'result.value'
                        val balanceHex = result as? String ?: "0x0"
                        val cleanHex = if (balanceHex.startsWith("0x")) {
                            balanceHex.substring(2)
                        } else {
                            balanceHex
                        }

                        val balanceWei = if (cleanHex.isEmpty()) {
                            BigInteger.ZERO
                        } else {
                            BigInteger(cleanHex, 16)
                        }

                        val balanceEth = balanceWei.toBigDecimal()
                            .divide(BigInteger.TEN.pow(18).toBigDecimal())

                        updateState {
                            it.copy(balance = String.format("%.6f ETH", balanceEth))
                        }
                    } catch (e: Exception) {
                        updateState { it.copy(balance = "Error parsing balance") }
                        showMessage("Error parsing balance: ${e.message}")
                    }
                }
            }
        }
    }

    private fun disconnectWallet() {
        try {
            ethereum.disconnect()
            updateState {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                    address = "",
                    balance = "0 ETH"
                )
            }
            showMessage("Disconnected successfully")
        } catch (e: Exception) {
            showMessage("Disconnect error: ${e.message}")
        }
    }

    fun clearEvent() {
        _uiEvent.value = null
    }
}
