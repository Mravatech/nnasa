package com.mnassa.domain.interactor

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 7/13/2018.
 */
interface NetworkInteractor {
    val isConnected: Boolean

    suspend fun awaitNetworkConnected()
    suspend fun awaitNetworkDisconnected()

    suspend fun isApiSupported(): ReceiveChannel<Boolean>

    fun register()
    fun unregister()

}