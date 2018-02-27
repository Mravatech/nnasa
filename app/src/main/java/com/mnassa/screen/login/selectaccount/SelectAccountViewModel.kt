package com.mnassa.screen.login.selectaccount

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
interface SelectAccountViewModel : MnassaViewModel {
    val showMessageChannel: BroadcastChannel<String>
}