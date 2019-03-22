package com.mnassa.core

import android.os.Bundle
import com.mnassa.core.addons.SubscriptionContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/20/2018.
 */
interface BaseViewModel : SubscriptionContainer {
    val setupErrorChannel: BroadcastChannel<String>

    fun onCreate(savedInstanceState: Bundle?)

    /**
     * This is where you should setup data
     * channels.
     */
    fun onSetup(setupScope: CoroutineScope)

    fun saveInstanceState(outBundle: Bundle)
    fun onCleared()
}