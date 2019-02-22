package com.mnassa.screen.splash

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/20/2018.
 */
interface SplashViewModel : MnassaViewModel {
    val openNextScreenChannel: BroadcastChannel<NextScreen>
    val showMessageChannel: BroadcastChannel<String>

    enum class NextScreen {
        LOGIN,
        MAIN
    }
}