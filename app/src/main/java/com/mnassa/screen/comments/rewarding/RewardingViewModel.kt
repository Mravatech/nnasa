package com.mnassa.screen.comments.rewarding

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/30/2018
 */
interface RewardingViewModel : MnassaViewModel {
    val defaultRewardChannel: BroadcastChannel<Long>
}