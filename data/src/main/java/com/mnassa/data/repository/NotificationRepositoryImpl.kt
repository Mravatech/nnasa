package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.DEFAULT_LIMIT
import com.mnassa.data.extensions.awaitList
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
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel

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

    private val preloadedOldNotifications = HashMap<String, Deferred<List<NotificationModel>>>()

    override suspend fun loadNewNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>> {
        val myUserId = requireNotNull(userRepository.getAccountIdOrNull())
        return db.child(TABLE_NOTIFICATIONS)
                .child(myUserId)
                .toValueChannelWithChangesHandling<NotificationDbEntity, NotificationModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = {
                            converter.convert(it, NotificationModel::class.java).also { it.isOld = false }
                        }
                )
    }

    override suspend fun loadOldNotifications(): ReceiveChannel<ListItemEvent<NotificationModel>> {
        val myUserId = requireNotNull(userRepository.getAccountIdOrNull())
        return db.child(TABLE_NOTIFICATIONS_OLD)
                .child(myUserId)
                .toValueChannelWithChangesHandling<NotificationDbEntity, NotificationModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { converter.convert(it, NotificationModel::class.java).also { it.isOld = true } }
                )
    }

    override suspend fun preloadOldNotifications(): List<NotificationModel> {
        val accountId = userRepository.getAccountIdOrException()
        val future = async {
            db.child(TABLE_NOTIFICATIONS_OLD)
                    .child(accountId)
                    .orderByChild("createdAt")
                    .limitToLast(DEFAULT_LIMIT)
                    .awaitList<NotificationDbEntity>(exceptionHandler)
                    .map { converter.convert(it, NotificationModel::class.java).also { it.isOld = true } }
                    .asReversed()
        }
        preloadedOldNotifications[accountId] = future
        return future.await()
    }

    override suspend fun getPreloadedOldNotifications(): List<NotificationModel> {
        return preloadedOldNotifications.getOrPut(userRepository.getAccountIdOrException()) { async { preloadOldNotifications() } }.await()
    }

    override suspend fun notificationView(resetCounter: Boolean, all: Boolean, ids: List<String>) {
        firebaseNotificationsApi.notificationView(NotificationViewRequest(
                resetCounter = resetCounter,
                all = all,
                ids = ids
        )).handleException(exceptionHandler)
    }

}