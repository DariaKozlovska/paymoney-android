package com.example.paymoney

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger

sealed class UIEvent {
    data class ShowMessage(val message: String) : UIEvent()
}

sealed class EventSink {
    object Connect : EventSink()
    object GetBalance : EventSink()
    object Disconnect : EventSink()
    object SignMessage : EventSink() // ✅ added for triggering from UI
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

    companion object {
        private const val TAG = "MainViewModel"
    }

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
                EventSink.SignMessage -> signMessage()
            }
        }
    }

    private fun connectWallet() {
        updateState { it.copy(isConnecting = true) }

        ethereum.connect { result ->
            viewModelScope.launch {
                when (result) {
                    is Result.Error -> {
                        updateState { it.copy(isConnecting = false) }
                        showMessage("Connection failed : ${result.error.message}")
                    }
                    is Result.Success -> {
                        Log.d(TAG, "Connection success: ${result}")
                        Log.d(TAG, "Result class: ${result::class.java}")
                        Log.d(TAG, "Result class: ${result::class.simpleName}")
                        val item = result as Result.Success.Item
                        val accounts = item.value
                        Log.d(TAG, "Unwrapped data: $accounts")
                        if (accounts?.isNotEmpty() == true) {
                            updateState {
                                it.copy(
                                    isConnecting = false,
                                    isConnected = true,
                                    address = accounts.toString()
                                )
                            }
                            showMessage("Connected successfully!")
                        } else {
                            updateState { it.copy(isConnecting = false) }
                            showMessage("Connection failed: No accounts returned")
                        }
                    }
                }
            }
        }
    }

    private fun signMessage() {
        val address = uiState.value.address
        if (address.isEmpty()) {
            showMessage("Not connected to wallet")
            return
        }

        val message = "Login to PayMoney App - ${System.currentTimeMillis()}"
        val hexMessage = "0x" + message.encodeToByteArray().toHex()

        val request = EthereumRequest(
            method = "personal_sign",
            params = listOf(address, hexMessage)
        )

        ethereum.sendRequest(request) { result ->
            viewModelScope.launch {
                when (result) {
                    is Result.Success -> {
                        val item = result as? Result.Success.Item
                        val signature = item?.value as? String
                        if (signature != null) {
                            Log.d(TAG, "Signature: $signature")
                            showMessage("Signature received!")
                        } else {
                            showMessage("No signature returned")
                        }
                    }

                    is Result.Error -> {
                        Log.e(TAG, "Signing error: ${result.error.message}")
                        showMessage("Signing failed: ${result.error.message}")
                    }
                }
            }
        }
    }

    private fun getBalance() {
        val address = uiState.value.address
        if (!uiState.value.isConnected || address.isEmpty()) {
            showMessage("Please connect wallet first")
            return
        }

        val request = EthereumRequest(
            method = "eth_getBalance",
            params = listOf(address, "latest")
        )

        ethereum.sendRequest(request) { result ->
            viewModelScope.launch {
                when (result) {
                    is Result.Error -> {
                        showMessage("Failed to get balance: ${result.error.message}")
                    }

                    is Result.Success -> {
                        val item = result as? Result.Success.Item
                        val balanceHex = item?.value as? String ?: "0x0"

                        try {
                            val cleanHex = balanceHex.removePrefix("0x")
                            val balanceWei = cleanHex.toBigIntegerOrNull(16) ?: BigInteger.ZERO
                            val balanceEth = balanceWei.toBigDecimal()
                                .divide(BigInteger.TEN.pow(18).toBigDecimal())

                            updateState {
                                it.copy(balance = String.format("%.6f ETH", balanceEth))
                            }
                        } catch (e: Exception) {
                            updateState { it.copy(balance = "Error") }
                            showMessage("Error parsing balance: ${e.message}")
                        }
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

    // ✅ ByteArray.toHex() extension
    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
