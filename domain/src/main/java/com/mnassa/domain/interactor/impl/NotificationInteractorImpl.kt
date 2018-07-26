package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.repository.NotificationRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationInteractorImpl(private val notificationRepository: NotificationRepository) : NotificationInteractor {

    override suspend fun loadOldNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>> {
        return produce {
            getPreloadedOldNotifications().forEach {
                send(ListItemEvent.Added(it))
            }
            notificationRepository.loadOldNotifications().consumeEach {
                send(it)
            }
        }
    }
    override suspend fun preloadOldNotifications(): List<NotificationModel> = notificationRepository.preloadOldNotifications()
    override suspend fun getPreloadedOldNotifications(): List<NotificationModel> = notificationRepository.getPreloadedOldNotifications()

    override suspend fun loadNewNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>> = notificationRepository.loadNewNotifications()
    override suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>) = notificationRepository.notificationView(resetCounter, all, ids)
}