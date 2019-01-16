package com.mnassa.domain.interactor

import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.domain.pagination.PaginationController
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 4/13/2018.
 */
interface EventsInteractor {
    val eventsPagination: PaginationController

    suspend fun loadAllImmediately(): List<EventModel>
    suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<List<EventModel>>>

    //group wall
    suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<EventModel>>
    suspend fun loadAllByGroupIdImmediately(groupId: String): List<EventModel>

    suspend fun createEvent(model: RawEventModel)
    suspend fun editEvent(model: RawEventModel)
    suspend fun changeStatus(event: EventModel, status: EventStatus)
    suspend fun promote(id: String)
    suspend fun getPromotePostPrice(): Long

    suspend fun loadByIdChannel(eventId: String): ReceiveChannel<EventModel?>
    suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>>
    /**
     * Same as [getTicketsChannel] but filtered by this
     * account's id.
     */
    suspend fun getBoughtTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>>

    suspend fun onItemViewed(item: EventModel)
    suspend fun onItemOpened(item: EventModel)
    suspend fun resetCounter()
    suspend fun getTickets(eventId: String): List<EventTicketModel>

    suspend fun getBoughtTicketsCount(eventId: String): Long
    suspend fun buyTickets(eventId: String, ticketsCount: Long)

    suspend fun getAttendedUsers(eventId: String): List<EventAttendee>
    suspend fun getAttendedUsersChannel(eventId: String): ReceiveChannel<List<EventAttendee>>
    suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>)
}