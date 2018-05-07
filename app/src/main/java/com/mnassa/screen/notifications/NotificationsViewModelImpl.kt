package com.mnassa.screen.notifications

import android.os.Bundle
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class NotificationsViewModelImpl(
        private val notificationInteractor: NotificationInteractor
) : MnassaViewModelImpl(), NotificationsViewModel {

    override val notificationChannel: BroadcastChannel<ListItemEvent<NotificationModel>> = BroadcastChannel(10)

    private val notificationOldChannel: BroadcastChannel<ListItemEvent<NotificationModel>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { notificationInteractor.loadNotificationsOld() })
    private val notificationNewChannel: BroadcastChannel<ListItemEvent<NotificationModel>> by ProcessAccountChangeArrayBroadcastChannel(
            receiveChannelProvider = { notificationInteractor.loadNotifications() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retrieveNotifications()
    }

    private fun retrieveNotifications() {
        handleException {
            notificationOldChannel.consumeEach {
                notificationChannel.send(it)
            }
        }
        handleException {
            notificationNewChannel.consumeEach {
                notificationChannel.send(it)
            }
        }
    }

    override fun notificationView(id: String) {
        handleException {
            notificationInteractor.notificationView(true, false, listOf(id))
        }
    }

}