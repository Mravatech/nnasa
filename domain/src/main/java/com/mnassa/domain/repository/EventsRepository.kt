package com.mnassa.domain.repository

import com.mnassa.domain.aggregator.AggregatorInEvent
import com.mnassa.domain.model.EventAttendee
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.domain.pagination.PaginationController
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 4/13/2018.
 */
interface EventsRepository {
    suspend fun preloadEvents(): List<EventModel>
    suspend fun getEventsFeedChannel(pagination: PaginationController): ReceiveChannel<AggregatorInEvent<EventModel>>
    suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<EventModel>>
    suspend fun loadAllByGroupIdImmediately(groupId: String): List<EventModel>

    suspend fun getEventsChannel(eventId: String): ReceiveChannel<EventModel?>
    suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>>
    suspend fun sendViewed(ids: List<String>)
    suspend fun sendOpened(ids: List<String>)
    suspend fun resetCounter()
    suspend fun getTickets(eventId: String): List<EventTicketModel>
    suspend fun buyTickets(eventId: String, ticketsCount: Long)
    suspend fun getAttendedUsers(eventId: String): List<EventAttendee>
    suspend fun getAttendedUsersChannel(eventId: String): ReceiveChannel<List<EventAttendee>>
    suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>)
    suspend fun createEvent(model: RawEventModel)
    suspend fun editEvent(model: RawEventModel)
    suspend fun getPromoteEventPrice(): Long?
    suspend fun promote(id: String)
}