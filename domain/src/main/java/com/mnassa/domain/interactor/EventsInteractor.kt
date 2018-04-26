package com.mnassa.domain.interactor

import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 4/13/2018.
 */
interface EventsInteractor {
    suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>>

    suspend fun createEvent(model: CreateOrEditEventModel)
    suspend fun editEvent(model: CreateOrEditEventModel)

    suspend fun loadByIdChannel(eventId: String): ReceiveChannel<EventModel?>
    suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>>

    suspend fun onItemViewed(item: EventModel)
    suspend fun getTickets(eventId: String): List<EventTicketModel>
    suspend fun canBuyTicket(eventId: String): Boolean
    suspend fun getBoughtTicketsCount(eventId: String): Long
    suspend fun buyTickets(eventId: String, ticketsCount: Long)

    suspend fun getAttendedUsers(eventId: String): List<EventAttendee>
    suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>)
}