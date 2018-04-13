package com.mnassa.screen.home

import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.extensions.ReConsumeWhenAccountChangedConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class HomeViewModelImpl(private val countersInteractor: CountersInteractor) : MnassaViewModelImpl(), HomeViewModel {
    override val unreadEventsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadEvents
    }
    override val unreadNeedsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadNeeds
    }

}