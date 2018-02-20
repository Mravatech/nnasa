package com.mnassa.screen.splash

import android.util.Log
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl : MnassaViewModelImpl, SplashViewModel {
    override val startup: BroadcastChannel<Int> = ConflatedBroadcastChannel()

    constructor() {
        Log.e("TEST", "VM Constructor")
    }

    override fun onCreated() {
        Log.e("TEST", "VM onCreated")

        launchCoroutineUI {

            (100 downTo 0).forEach {
                Log.e("TEST", "VM countdown: $it")

                startup.send(it)
                delay(5000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "VM onCleared()")
    }
}