package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.extensions.*
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseEventsApi
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.firebase.EventAttendeeAccountDbEntity
import com.mnassa.data.network.bean.firebase.EventDbEntity
import com.mnassa.data.network.bean.firebase.EventTicketDbEntity
import com.mnassa.data.network.bean.firebase.PriceDbEntity
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.EventAttendee
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.domain.repository.EventsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 4/13/2018.
 */
class EventsRepositoryImpl(private val firestore: FirebaseFirestore,
                           private val userRepository: UserRepository,
                           private val exceptionHandler: ExceptionHandler,
                           private val converter: ConvertersContext,
                           private val postApi: FirebasePostApi,
                           private val eventsApi: FirebaseEventsApi,
                           private val db: DatabaseReference) : EventsRepository {

    override suspend fun loadAllImmediately(): List<EventModel> {
        return firestoreLockSuspend {
            firestore
                    .collection(DatabaseContract.TABLE_EVENTS)
                    .document(userRepository.getAccountIdOrException())
                    .collection(DatabaseContract.TABLE_EVENTS_COLLECTION_FEED)
                    .awaitList<EventDbEntity>()
                    .map { converter.convert(it, EventModel::class.java) }
        }
    }

    override suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>> {
        return firestoreLockSuspend {
            firestore
                    .collection(DatabaseContract.TABLE_EVENTS)
                    .document(userRepository.getAccountIdOrException())
                    .collection(DatabaseContract.TABLE_EVENTS_COLLECTION_FEED)
                    .toValueChannelWithChangesHandling<EventDbEntity, EventModel>(
                            exceptionHandler = exceptionHandler,
                            mapper = { converter.convert(it) }
                    )
        }
    }

    override suspend fun getEventsChannel(eventId: String): ReceiveChannel<EventModel?> {
        return firestoreLockSuspend {
            firestore
                    .collection(DatabaseContract.TABLE_EVENTS)
                    .document(userRepository.getAccountIdOrException())
                    .collection(DatabaseContract.TABLE_EVENTS_COLLECTION_FEED)
                    .document(eventId)
                    .toValueChannel<EventDbEntity>(
                            exceptionHandler = exceptionHandler
                    ).map { it?.let { converter.convert(it, EventModel::class.java) } }
        }
    }

    override suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_EVENT_TICKETS)
                    .document(eventId)
                    .toListChannel<EventTicketDbEntity>(exceptionHandler)
                    .map { converter.convertCollection(it, EventTicketModel::class.java) }
        }
    }

    override suspend fun sendViewed(ids: List<String>) {
        postApi.viewItems(ViewItemsRequest(ids, NetworkContract.EntityType.EVENT)).handleException(exceptionHandler)
    }

    override suspend fun sendOpened(ids: List<String>) {
        postApi.openItem(OpenItemsRequest(ids.first(), NetworkContract.EntityType.EVENT)).handleException(exceptionHandler)
    }

    override suspend fun resetCounter() {
        postApi.resetCounter(ResetCounterRequest(NetworkContract.ResetCounter.EVENTS)).handleException(exceptionHandler)
    }

    override suspend fun getTickets(eventId: String): List<EventTicketModel> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_EVENT_TICKETS)
                    .document(eventId)
                    .awaitList<EventTicketDbEntity>()
                    .run { converter.convertCollection(this, EventTicketModel::class.java) }
        }
    }

    override suspend fun buyTickets(eventId: String, ticketsCount: Long) {
        eventsApi.buyTickets(BuyTicketsRequest(eventId, ticketsCount)).handleException(exceptionHandler)
    }

    override suspend fun getAttendedUsers(eventId: String): List<EventAttendee> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_EVENT_ATTENDIES)
                    .document(eventId)
                    .collection(DatabaseContract.TABLE_EVENT_ATTENDIES_COLLECTION)
                    .awaitList<EventAttendeeAccountDbEntity>()
                    .map { EventAttendee(converter.convert(it), it.presence ?: false) }
        }
    }

    override suspend fun getAttendedUsersChannel(eventId: String): ReceiveChannel<List<EventAttendee>> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_EVENT_ATTENDIES)
                    .document(eventId)
                    .collection(DatabaseContract.TABLE_EVENT_ATTENDIES_COLLECTION)
                    .toListChannel<EventAttendeeAccountDbEntity>(exceptionHandler)
                    .map { it.map { EventAttendee(converter.convert(it), it.presence ?: false) } }
        }
    }

    override suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>) {
        val attendees = ArrayList<EventAttendeeBean>()
        presentUsers.mapTo(attendees) { EventAttendeeBean(it, true) }
        notPresentUsers.mapTo(attendees) { EventAttendeeBean(it, false) }
        eventsApi.saveAttendee(EventAttendeeRequest(eventId, attendees)).handleException(exceptionHandler)
    }

    override suspend fun createEvent(model: RawEventModel) {
        val request: CreateOrEditEventRequest = converter.convert(model)
        eventsApi.createEvent(request).handleException(exceptionHandler)
    }

    override suspend fun editEvent(model: RawEventModel) {
        val request: CreateOrEditEventRequest = converter.convert(model)
        eventsApi.editEvent(request).handleException(exceptionHandler)
    }

    override suspend fun getPromoteEventPrice(): Long? {
        return db.child(DatabaseContract.PROMOTE_EVENT)
                .await<PriceDbEntity>(exceptionHandler)
                ?.takeIf { it.state }
                ?.amount
    }

    override suspend fun promote(id: String) {
        postApi.promote(PromotePostRequest(id, NetworkContract.EntityType.EVENT)).handleException(exceptionHandler)
    }
}