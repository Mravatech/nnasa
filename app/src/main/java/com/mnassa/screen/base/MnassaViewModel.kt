package com.mnassa.screen.base

import com.mnassa.core.BaseViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/20/2018.
 */
interface MnassaViewModel : BaseViewModel {
    val isProgressEnabledChannel: BroadcastChannel<ProgressEvent>
}