package com.mnassa.screen.notifications

import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class NotificationsViewModelImpl(
        private val notificationInteractor: NotificationInteractor
) : MnassaViewModelImpl(), NotificationsViewModel {

    override val newNotificationChannel: BroadcastChannel<ListItemEvent<List<NotificationModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { notificationInteractor.loadNewNotifications() })

    override val oldNotificationChannel: BroadcastChannel<ListItemEvent<List<NotificationModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            receiveChannelProvider = { notificationInteractor.loadOldNotifications() })

    override fun notificationView(id: String) {
        handleException {
            notificationInteractor.notificationView(true, false, listOf(id))
        }
    }

}