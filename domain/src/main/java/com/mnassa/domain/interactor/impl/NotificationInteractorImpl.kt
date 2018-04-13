package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.repository.NotificationRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationInteractorImpl(private val notificationRepository: NotificationRepository) : NotificationInteractor {

    override suspend fun loadNotificationsOld(): ReceiveChannel<ListItemEvent<NotificationModel>> = notificationRepository.loadNotificationsOld()

    override suspend fun loadNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>> = notificationRepository.loadNotifications()

    override suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>) {
        notificationRepository.notificationView(resetCounter, all, ids)
    }
}