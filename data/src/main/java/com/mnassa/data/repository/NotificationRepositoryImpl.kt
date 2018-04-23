package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.network.api.FirebaseNotificationsApi
import com.mnassa.data.network.bean.firebase.NotificationDbEntity
import com.mnassa.data.network.bean.retrofit.request.NotificationViewRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.repository.DatabaseContract.TABLE_NOTIFICATIONS
import com.mnassa.data.repository.DatabaseContract.TABLE_NOTIFICATIONS_OLD
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.repository.NotificationRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */

class NotificationRepositoryImpl(private val db: DatabaseReference,
                                 private val firebaseNotificationsApi: FirebaseNotificationsApi,
                                 private val userRepository: UserRepository,
                                 private val exceptionHandler: ExceptionHandler,
                                 private val converter: ConvertersContext) : NotificationRepository {

    override suspend fun loadNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>> {
        val myUserId = requireNotNull(userRepository.getAccountIdOrNull())
        return db.child(TABLE_NOTIFICATIONS)
                .child(myUserId)
                .toValueChannelWithChangesHandling<NotificationDbEntity, NotificationModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { converter.convert(it, NotificationModel::class.java) }
                )
                .map {
                    it.item.isOld = false
                    it
                }
    }

    override suspend fun loadNotificationsOld(): ReceiveChannel<ListItemEvent<NotificationModel>> {
        val myUserId = requireNotNull(userRepository.getAccountIdOrNull())
        return db.child(TABLE_NOTIFICATIONS_OLD)
                .child(myUserId)
                .toValueChannelWithChangesHandling<NotificationDbEntity, NotificationModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { converter.convert(it, NotificationModel::class.java) }
                )
    }

    override suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>) {
        firebaseNotificationsApi.notificationView(NotificationViewRequest(
                resetCounter = resetCounter,
                all = all,
                ids = ids
        )).handleException(exceptionHandler)
    }

}