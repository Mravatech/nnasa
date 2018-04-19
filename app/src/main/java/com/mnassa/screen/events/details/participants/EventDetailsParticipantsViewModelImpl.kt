package com.mnassa.screen.events.details.participants

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsParticipantsViewModelImpl(private val eventId: String,
                                            private val eventsInteractor: EventsInteractor,
                                            private val userProfileInteractor: UserProfileInteractor,
                                            private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), EventDetailsParticipantsViewModel {

    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()
    override val participantsChannel: ConflatedBroadcastChannel<List<EventParticipant>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                if (it != null) {
                    eventChannel.send(it)
                } else {
                    //TODO
                }
            }
        }

        handleException {
            eventsInteractor.getTicketsChannel(eventId).consumeEach {
                participantsChannel.send(it.mapNotNull { convertTicketToParticipant(it) })
            }
        }
    }

    private suspend fun convertTicketToParticipant(ticket: EventTicketModel): EventParticipant? {
        val user = userProfileInteractor.getProfileById(ticket.ownerId) ?: return null
        val isConnections = connectionsInteractor.getConnectionStatusById(ticket.ownerId) == ConnectionStatus.CONNECTED
        return EventParticipant(user, isConnections, ticket.ticketCount.toInt() - 1)
    }

}