package com.mnassa.screen.notifications

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
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

    override fun retrieveNotifications() {
        handleException {
            launchCoroutineUI {
                notificationInteractor.loadNotifications().consumeEach {
                    notificationChannel.send(it)
                }
            }
            launchCoroutineUI {
                notificationInteractor.loadNotificationsOld().consumeEach {
                    notificationChannel.send(it)
                }
            }
        }
    }

}