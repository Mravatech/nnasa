package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.repository.EventsRepository
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

/**
 * Created by Peter on 4/13/2018.
 */
class EventsInteractorImpl(private val eventsRepository: EventsRepository, private val userProfileInteractor: UserProfileInteractor) : EventsInteractor {
    private val viewItemChannel = ArrayChannel<ListItemEvent<EventModel>>(10)

    init {
        launch {
            viewItemChannel.bufferize(SubscriptionsContainerDelegate(), SEND_VIEWED_ITEMS_BUFFER_DELAY).consumeEach {
                if (it.item.isNotEmpty()) {
                    try {
                        eventsRepository.sendViewed(it.item.map { it.id })
                    } catch (e: Exception) {
                        Timber.d(e)
                    }
                }
            }
        }
    }

    override suspend fun onItemViewed(item: EventModel) {
        if (item.author.id == userProfileInteractor.getAccountIdOrException()) {
            return
        }
        viewItemChannel.send(ListItemEvent.Added(item))
    }

    override suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>> {
        return eventsRepository.getEventsFeedChannel()
    }

    override suspend fun loadByIdChannel(eventId: String): ReceiveChannel<EventModel?> {
        return eventsRepository.getEventsChannel(eventId)
    }

    override suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>> {
        return eventsRepository.getTicketsChannel(eventId)
    }

    override suspend fun getTickets(eventId: String): List<EventTicketModel> {
        return eventsRepository.getTickets(eventId)
    }

    override suspend fun canBuyTicket(eventId: String): Boolean {
        val event = loadByIdChannel(eventId).consume { receive() } ?: return false
        if (event.ticketsSold >= event.ticketsTotal || event.status != EventStatus.OPENED) return false
        return getBoughtTicketsCount(eventId) < event.ticketsPerAccount
    }

    override suspend fun getBoughtTicketsCount(eventId: String): Long {
        val userId = userProfileInteractor.getAccountIdOrNull() ?: return 0L
        var counter = 0L
        getTickets(eventId).forEach {
            if (it.ownerId == userId) {
                counter += it.ticketCount
            }
        }
        return counter
    }

    override suspend fun buyTickets(eventId: String, ticketsCount: Long) {
        eventsRepository.buyTickets(eventId, ticketsCount)
    }

    override suspend fun getAttendedUsers(eventId: String): List<EventAttendee> {
        return eventsRepository.getAttendedUsers(eventId)
    }

    override suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>) {
        eventsRepository.saveAttendedUsers(eventId, presentUsers, notPresentUsers)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}