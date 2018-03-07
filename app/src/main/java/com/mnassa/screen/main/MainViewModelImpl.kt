package com.mnassa.screen.main

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(
        private val loginInteractor: LoginInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val countersInteractor: CountersInteractor) : MnassaViewModelImpl(), MainViewModel {
    override val openScreenChannel: ArrayBroadcastChannel<MainViewModel.ScreenType> = ArrayBroadcastChannel(10)

    override val unreadChatsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val unreadNotificationsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val unreadConnectionsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    private val unreadEventsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    private val unreadNeedsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val unreadEventsAndNeedsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            countersInteractor.numberOfUnreadChats.consumeEach {
                unreadChatsCountChannel.send(it)
            }
        }

        launchCoroutineUI {
            countersInteractor.numberOfUnreadNotifications.consumeEach {
                unreadNotificationsCountChannel.send(it)
            }
        }

        launchCoroutineUI {
            countersInteractor.numberOfRequested.consumeEach {
                unreadConnectionsCountChannel.send(it)
            }
        }

        launchCoroutineUI {
            countersInteractor.numberOfUnreadEvents.consumeEach {
                unreadEventsCountChannel.send(it)

                unreadEventsAndNeedsCountChannel.send((unreadNeedsCountChannel.valueOrNull
                        ?: 0) + it)
            }
        }

        launchCoroutineUI {
            countersInteractor.numberOfUnreadNeeds.consumeEach {
                unreadNeedsCountChannel.send(it)

                unreadEventsAndNeedsCountChannel.send((unreadEventsCountChannel.valueOrNull
                        ?: 0) + it)
            }
        }

    }

    override fun logout() {
        handleException {
            loginInteractor.signOut()
            openScreenChannel.send(MainViewModel.ScreenType.LOGIN)
        }
    }
}