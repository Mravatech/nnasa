package com.mnassa.screen.events.details.participants

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.extensions.isMyEvent
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsParticipantsViewModelImpl(private val eventId: String,
                                            private val eventsInteractor: EventsInteractor,
                                            private val userProfileInteractor: UserProfileInteractor,
                                            private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), EventDetailsParticipantsViewModel {

    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()
    override val participantsChannel: ConflatedBroadcastChannel<List<EventParticipantItem>> = ConflatedBroadcastChannel()

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

        loadTickets()
    }

    override suspend fun saveParticipants(participants: List<EventParticipantItem>) {

        handleExceptionsSuspend {
            withProgressSuspend {
                val users = participants.mapNotNull { (it as? EventParticipantItem.User) }

                eventsInteractor.saveAttendedUsers(
                        eventId = eventId,
                        presentUsers = users.filter { it.isChecked }.map { it.user.id },
                        notPresentUsers = users.filter { !it.isChecked }.map { it.user.id })
            }
        }

        loadTickets()
    }

    private var loadTicketsJob: Job? = null
    private fun loadTickets() {
        loadTicketsJob?.cancel()
        loadTicketsJob = handleException {
            eventsInteractor.getTicketsChannel(eventId).consumeEach {
                async {
                    val attendedUsers = eventsInteractor.getAttendedUsers(eventId).mapNotNullTo(HashSet()) { it.takeIf { it.isPresent }?.user?.id }

                    var hasConnections = false
                    var hasOtherUsers = false
                    val participants = it.mapNotNullTo(ArrayList<EventParticipantItem>(it.size)) {
                        convertTicketToParticipant(it, attendedUsers)?.also {
                            hasConnections = hasConnections || it.isInConnections
                            hasOtherUsers = hasOtherUsers || !it.isInConnections
                        }
                    }
                    if (hasConnections) {
                        val event = eventChannel.openSubscription().consume { receive() }
                        participants += EventParticipantItem.ConnectionsHeader(event.isMyEvent())
                    }
                    if (hasOtherUsers) {
                        participants += EventParticipantItem.OtherHeader
                    }
                    participantsChannel.send(participants)
                }.await()
            }
        }
    }

    private suspend fun convertTicketToParticipant(ticket: EventTicketModel, attendedUsers: Set<String>): EventParticipantItem.User? {
        val user = userProfileInteractor.getProfileById(ticket.ownerId) ?: return null
        val isConnections = connectionsInteractor.getConnectionStatusById(ticket.ownerId) == ConnectionStatus.CONNECTED
        return EventParticipantItem.User(user, isConnections, maxOf(ticket.ticketCount.toInt() - 1, 0), isChecked = attendedUsers.contains(user.id))
    }

}