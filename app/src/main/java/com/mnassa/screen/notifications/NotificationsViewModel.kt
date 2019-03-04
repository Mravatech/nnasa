package com.mnassa.screen.notifications

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface NotificationsViewModel : MnassaViewModel {
    val oldNotificationChannel: BroadcastChannel<ListItemEvent<List<NotificationModel>>>
    val newNotificationChannel: BroadcastChannel<ListItemEvent<List<NotificationModel>>>

    fun notificationView(id: String)
    fun resetCounter()
}