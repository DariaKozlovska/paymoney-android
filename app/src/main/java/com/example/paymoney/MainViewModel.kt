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

sealed class UIEvent {
    data class ShowMessage(val message: String) : UIEvent()
}

sealed class EventSink {
    object Connect : EventSink()
    object Disconnect : EventSink()
    object SignMessage : EventSink()
}

data class MainUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val isSigned: Boolean = false,
    val balance: String = "0 ETH",
    val address: String = "",
    val chainId: String = "",
    val shouldLaunchMetaMask: Boolean = false
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
                        showMessage("Connection failed: ${result.error.message}")
                    }
                    is Result.Success -> {
                        val item = result as Result.Success.Item
                        val accounts = item.value
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
                            showMessage("No accounts returned")
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
                            updateState { it.copy(isSigned = true) }
                        } else {
                            showMessage("No signature returned")
                            updateState { it.copy(isSigned = false) }
                        }
                    }

                    is Result.Error -> {
                        Log.e(TAG, "Signing error: ${result.error.message}")
                        showMessage("Signing failed: ${result.error.message}")
                        updateState { it.copy(isSigned = false) }
                    }
                }
            }
        }
    }

    private fun getChainId() {
        val address = uiState.value.address

        ethereum.sendRequest (request = EthereumRequest(
            method = "eth_chainId",
            params = listOf(address, "latest")
        )) { result ->
            viewModelScope.launch {
                when (result) {
                    is Result.Success -> {
                        val item = result as? Result.Success.Item
                        val chainIds = item?.value as? String
                        showMessage("Chain ID: $chainIds")
                        Log.d("MetaMask", "Chain ID: $chainIds")
                        updateState {
                            it.copy(
                                chainId = chainIds.toString()
                            )
                        }

                    }
                    is Result.Error -> {
                        showMessage("Chain ID Error: ${result.error.message}")
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
                    balance = "0 ETH",
                    chainId = "",
                    isSigned = false
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

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}