package com.mnassa.screen.main

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface MainViewModel : MnassaViewModel {
    val openScreenChannel: BroadcastChannel<ScreenType>

    fun logout()

    enum class ScreenType {
        LOGIN
    }
}