package com.mnassa.screen.comments.rewarding

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/30/2018
 */
class RewardingViewModelImpl(private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), RewardingViewModel {
    override val defaultRewardChannel: BroadcastChannel<Long> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            withProgressSuspend {
                val defaultCount = walletInteractor.getDefaultRewardingPoints()
                defaultRewardChannel.send(defaultCount)
            }
        }
    }

}