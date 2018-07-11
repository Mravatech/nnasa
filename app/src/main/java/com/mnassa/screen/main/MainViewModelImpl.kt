package com.mnassa.screen.main

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.exception.NotAuthorizedException
import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(
        private val loginInteractor: LoginInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val notificationInteractor: NotificationInteractor,
        private val countersInteractor: CountersInteractor
) : MnassaViewModelImpl(), MainViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<MainViewModel.ScreenType> = ArrayBroadcastChannel(10)

    override val unreadChatsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadChats
    }
    override val unreadNotificationsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadNotifications
    }
    override val unreadConnectionsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel {
        countersInteractor.numberOfRequested
    }
    private val unreadEventsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel(
            receiveChannelProvider = { countersInteractor.numberOfUnreadEvents }
    )
    private val unreadNeedsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel(
            receiveChannelProvider = { countersInteractor.numberOfUnreadNeeds }
    )
    override val unreadEventsAndNeedsCountChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel(0)

    override val currentAccountChannel: ConflatedBroadcastChannel<ShortAccountModel> by ProcessAccountChangeConflatedBroadcastChannel {
        val accountId = userProfileInteractor.getAccountIdOrException()
        userProfileInteractor.getAccountByIdChannel(accountId).map {
            it
                    ?: throw NotAuthorizedException("User with account $accountId not found!", NullPointerException())
        }
    }
    override val availableAccountsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            userProfileInteractor.getAllAccounts().consumeTo(availableAccountsChannel)
        }

        var unreadEventsCount = 0
        var unreadNeedsCount = 0

        handleException {
            unreadEventsCountChannel.consumeEach {
                unreadEventsCount = it
                unreadEventsAndNeedsCountChannel.send(unreadNeedsCount + it)
            }
        }

        handleException {
            unreadNeedsCountChannel.consumeEach {
                unreadNeedsCount = it
                unreadEventsAndNeedsCountChannel.send(unreadEventsCount + it)
            }
        }
    }

    override fun selectAccount(account: ShortAccountModel) {
        handleException {
            try {
                withProgressSuspend {
                    userProfileInteractor.setCurrentUserAccount(account)
                    delay(1_000) //for animation purpose. Also this time is needed to update all counters
                }
            } catch (e: Exception) {
                currentAccountChannel.valueOrNull?.apply { currentAccountChannel.send(this) }
                throw e
            }
        }
    }

    override fun logout() {
        handleException {
            loginInteractor.signOut(LogoutReason.ManualLogout())
        }
    }

    override fun resetAllNotifications() {
        handleException {
            notificationInteractor.notificationView(true, true, emptyList())
        }
    }
}