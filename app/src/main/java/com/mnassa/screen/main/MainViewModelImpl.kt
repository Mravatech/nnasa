package com.mnassa.screen.main

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.ReConsumeWhenAccountChangedConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(
        private val loginInteractor: LoginInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val countersInteractor: CountersInteractor
) : MnassaViewModelImpl(), MainViewModel {
    override val openScreenChannel: ArrayBroadcastChannel<MainViewModel.ScreenType> = ArrayBroadcastChannel(10)

    override val unreadChatsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadChats
    }
    override val unreadNotificationsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadNotifications
    }
    override val unreadConnectionsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        countersInteractor.numberOfRequested
    }
    private val unreadEventsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel(
            receiveChannelProvider = { countersInteractor.numberOfUnreadEvents },
            onEachEvent = {
                unreadEventsAndNeedsCountChannel.send((unreadNeedsCountChannel.valueOrNull
                        ?: 0) + it)
            }
    )
    private val unreadNeedsCountChannel: ConflatedBroadcastChannel<Int> by ReConsumeWhenAccountChangedConflatedBroadcastChannel(
            receiveChannelProvider = { countersInteractor.numberOfUnreadNeeds },
            onEachEvent = {
                unreadEventsAndNeedsCountChannel.send((unreadEventsCountChannel.valueOrNull
                        ?: 0) + it)
            }
    )
    override val unreadEventsAndNeedsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()

    override val currentAccountChannel: ConflatedBroadcastChannel<ShortAccountModel> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        userProfileInteractor.currentProfile.openSubscription()
    }
    override val availableAccountsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            userProfileInteractor.getAllAccounts().consumeTo(availableAccountsChannel)
        }
    }

    override fun selectAccount(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                userProfileInteractor.setCurrentUserAccount(account)
                delay(1_000)
            }
        }
    }

    override fun logout() {
        handleException {
            loginInteractor.signOut()
        }
    }
}