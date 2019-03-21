package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.model.withBuffer
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.produce

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationInteractorImpl(
    private val appInfoProvider: AppInfoProvider,
    private val notificationRepository: NotificationRepository
) : NotificationInteractor {

    override suspend fun loadOldNotifications(): ReceiveChannel<ListItemEvent<List<NotificationModel>>> {
        return GlobalScope.produce(Dispatchers.Unconfined) {
            send(ListItemEvent.Added(getPreloadedOldNotifications()))
            notificationRepository.loadOldNotifications()
                .withBuffer(bufferWindow = LOAD_NOTIFICATIONS_BUFFER_WINDOW)
                .consumeEach {
                    send(it)
                }
        }
    }

    override suspend fun preloadOldNotifications(): List<NotificationModel> = notificationRepository.preloadOldNotifications()
    override suspend fun getPreloadedOldNotifications(): List<NotificationModel> = notificationRepository.getPreloadedOldNotifications()

    override suspend fun loadNewNotifications(): ReceiveChannel<ListItemEvent<List<NotificationModel>>> = notificationRepository.loadNewNotifications().map { it.toBatched() }
    override suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>) {
        if (appInfoProvider.isGhost) return
        notificationRepository.notificationView(resetCounter, all, ids)
    }

    companion object {
        private const val LOAD_NOTIFICATIONS_BUFFER_WINDOW = 500L
    }
}