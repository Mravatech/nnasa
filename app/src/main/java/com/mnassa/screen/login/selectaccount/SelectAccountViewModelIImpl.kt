package com.mnassa.screen.login.selectaccount

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountViewModelIImpl : MnassaViewModelImpl(), SelectAccountViewModel {

    override val showMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            for (i in 1..5) {
                showMessageChannel.send("Hello $i")
                delay(1_000)
            }
        }
    }
}