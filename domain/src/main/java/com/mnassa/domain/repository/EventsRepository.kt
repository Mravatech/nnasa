package com.mnassa.domain.repository

import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 4/13/2018.
 */
interface EventsRepository {
    suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>>
    suspend fun getEventsChannel(eventId: String): ReceiveChannel<EventModel?>
    suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>>
    suspend fun sendViewed(ids: List<String>)
    suspend fun getTickets(eventId: String): List<EventTicketModel>
    suspend fun buyTickets(eventId: String, ticketsCount: Long)
    suspend fun getAttendedUsers(eventId: String): List<EventAttendee>
    suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>)
    suspend fun createEvent(model: CreateOrEditEventModel)
    suspend fun editEvent(model: CreateOrEditEventModel)
}