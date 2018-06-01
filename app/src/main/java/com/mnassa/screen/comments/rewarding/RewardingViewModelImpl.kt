package com.mnassa.screen.comments.rewarding

import android.os.Bundle
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/30/2018
 */
class RewardingViewModelImpl(private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), RewardingViewModel {
    override val defaultRewardChannel: BroadcastChannel<Long> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            withProgressSuspend {
                val defaultCount = walletInteractor.getDefaultRewardingPoints()
                defaultRewardChannel.send(defaultCount)
            }
        }
    }

}