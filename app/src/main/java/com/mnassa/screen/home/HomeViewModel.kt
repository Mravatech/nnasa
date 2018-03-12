package com.mnassa.screen.home

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface HomeViewModel : MnassaViewModel {
    val unreadEventsCountChannel: BroadcastChannel<Int>
    val unreadNeedsCountChannel: BroadcastChannel<Int>
}