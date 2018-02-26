package com.mnassa.screen.splash

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/20/2018.
 */
interface SplashViewModel : MnassaViewModel {
    val progressChannel: BroadcastChannel<Int>
    val showMessageChannel: BroadcastChannel<String>

    suspend fun isLoggedIn(): Boolean
}