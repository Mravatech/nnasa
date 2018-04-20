package com.mnassa.domain.repository

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.ShortAccountModel
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
    suspend fun getAttendedUsers(eventId: String): List<ShortAccountModel>
}