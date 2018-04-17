package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.network.bean.firebase.EventDbEntity
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.repository.EventsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 4/13/2018.
 */
class EventsRepositoryImpl(private val firestore: FirebaseFirestore,
                           private val userRepository: UserRepository,
                           private val exceptionHandler: ExceptionHandler,
                           private val converter: ConvertersContext) : EventsRepository {

    override suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>> {
        return firestore
                .collection(DatabaseContract.TABLE_EVENTS)
                .document(userRepository.getAccountIdOrException())
                .collection(DatabaseContract.TABLE_EVENTS_COLLECTION_FEED)
                .toValueChannelWithChangesHandling<EventDbEntity, EventModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { converter.convert(it) }
                )
    }

    override suspend fun getEventsWallChannel(): ReceiveChannel<ListItemEvent<EventModel>> {
        TODO()
    }
}