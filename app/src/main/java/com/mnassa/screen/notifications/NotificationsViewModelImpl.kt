package com.mnassa.screen.notifications

import com.bluelinelabs.conductor.Controller
import com.mnassa.core.addons.launchWorker
import com.mnassa.core.addons.launchWorkerNoExceptions
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.deeplink.DeeplinkHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class NotificationsViewModelImpl(
        private val deeplinkHandler: DeeplinkHandler,
        private val notificationInteractor: NotificationInteractor
) : MnassaViewModelImpl(), NotificationsViewModel {

    override val newNotificationChannel: BroadcastChannel<ListItemEvent<List<NotificationModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { notificationInteractor.loadNewNotifications() })

    override val oldNotificationChannel: BroadcastChannel<ListItemEvent<List<NotificationModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            receiveChannelProvider = { notificationInteractor.loadOldNotifications() })

    override val openController: BroadcastChannel<Controller> = BroadcastChannel(1)

    override fun openNotification(n: NotificationModel) {
        if (!n.isOld) {
            markNotificationViewed(n)
        }

        launchWorker {
            withProgressSuspend {
                val controller = deeplinkHandler.handle(n)
                if (controller != null) {
                    openController.send(controller)
                }
            }
        }
    }

    override fun markNotificationViewed(n: NotificationModel) {
        GlobalScope.launchWorkerNoExceptions {
            notificationInteractor.notificationView(true, false, listOf(n.id))
        }
    }

    override fun resetCounter() {
        GlobalScope.launchWorkerNoExceptions {
            notificationInteractor.notificationView(true, true, emptyList())
        }
    }
}