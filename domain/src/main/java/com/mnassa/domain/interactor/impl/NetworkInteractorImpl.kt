package com.mnassa.domain.interactor.impl

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.await
import com.mnassa.domain.interactor.NetworkInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.repository.ClientDataRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map


/**
 * Created by Peter on 7/13/2018.
 */
class NetworkInteractorImpl(private val context: Context,
                            private val clientDataRepository: ClientDataRepository,
                            private val appInfoProvider: AppInfoProvider) : BroadcastReceiver(), NetworkInteractor {
    private val connectedStateExecutor by lazy { StateExecutor<Boolean, Boolean>(isConnected) { it } }
    private val disconnectedStateExecutor by lazy { StateExecutor<Boolean, Boolean>(!isConnected) { !it } }

    override val isConnected: Boolean
        @SuppressLint("MissingPermission")
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }

    override suspend fun awaitNetworkConnected() = connectedStateExecutor.await().run { Unit }

    override suspend fun awaitNetworkDisconnected() = disconnectedStateExecutor.await().run { Unit }

    override fun isApiSupported(): ReceiveChannel<Boolean> {
        return clientDataRepository.getMinimumSupportedApiVersion().map { minApiVersion ->
            minApiVersion == null || appInfoProvider.versionCode >= minApiVersion
        }
    }

    override fun register() = context.registerReceiver(this, getFilter()).run { Unit }

    override fun unregister() = context.unregisterReceiver(this)

    override fun onReceive(context: Context, intent: Intent?) {
        val isConnectedLocal = isConnected
        connectedStateExecutor.value = isConnectedLocal
        disconnectedStateExecutor.value = !isConnectedLocal
    }

    private fun getFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        return filter
    }
}