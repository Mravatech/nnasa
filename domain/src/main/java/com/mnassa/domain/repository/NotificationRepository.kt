package com.mnassa.domain.repository

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */

interface NotificationRepository {
    suspend fun loadOldNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>>
    suspend fun preloadOldNotifications(): List<NotificationModel>
    suspend fun getPreloadedOldNotifications(): List<NotificationModel>

    suspend fun loadNewNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>>

    suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>)
}