package com.example.paymoney.metamask

import android.content.Context
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.Ethereum

object AppModule {

    private var ethereumInstance: Ethereum? = null

    fun provideEthereum(context: Context): Ethereum {
        return ethereumInstance ?: run {
            val dappMetadata = DappMetadata(
                name = "PayMoney App",
                url = "https://paymoney.com",
                iconUrl = "https://cdn.sstatic.net/Sites/stackoverflow/Img/apple-touch-icon.png"
            )

            val ethereum = Ethereum(context, dappMetadata)
            ethereumInstance = ethereum
            ethereum
        }
    }
}