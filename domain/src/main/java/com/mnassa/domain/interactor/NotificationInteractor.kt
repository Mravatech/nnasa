package com.mnassa.domain.interactor

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
interface NotificationInteractor {
    suspend fun loadOldNotifications(): ReceiveChannel<ListItemEvent<List<NotificationModel>>>
    suspend fun preloadOldNotifications(): List<NotificationModel>
    suspend fun getPreloadedOldNotifications(): List<NotificationModel>

    suspend fun loadNewNotifications(): ReceiveChannel<ListItemEvent<List<NotificationModel>>>

    suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>)
}