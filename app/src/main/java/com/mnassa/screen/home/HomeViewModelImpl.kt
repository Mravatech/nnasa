package com.mnassa.screen.home

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class HomeViewModelImpl(private val countersInteractor: CountersInteractor) : MnassaViewModelImpl(), HomeViewModel {
    override val unreadEventsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val unreadNeedsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            countersInteractor.numberOfUnreadEvents.consumeEach {
                unreadEventsCountChannel.send(it)
            }
        }

        launchCoroutineUI {
            countersInteractor.numberOfUnreadNeeds.consumeEach {
                unreadNeedsCountChannel.send(it)
            }
        }
    }
}