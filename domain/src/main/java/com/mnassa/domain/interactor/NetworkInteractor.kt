package com.mnassa.domain.interactor

/**
 * Created by Peter on 7/13/2018.
 */
interface NetworkInteractor {
    val isConnected: Boolean

    suspend fun awaitNetworkConnected()
    suspend fun awaitNetworkDisconnected()

    fun register()
    fun unregister()

}