package com.mnassa.screen.notifications

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface NotificationsViewModel : MnassaViewModel {
    val notificationChannel: BroadcastChannel<ListItemEvent<NotificationModel>>

    fun notificationView(id: String)
}