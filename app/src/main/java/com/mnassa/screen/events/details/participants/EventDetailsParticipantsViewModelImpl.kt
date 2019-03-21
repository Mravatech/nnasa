package com.mnassa.screen.events.details.participants

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.extensions.canMarkParticipants
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsParticipantsViewModelImpl(private val eventId: String,
                                            private val event: EventModel,
                                            private val eventsInteractor: EventsInteractor,
                                            private val userProfileInteractor: UserProfileInteractor,
                                            private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), EventDetailsParticipantsViewModel {

    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()
    override val participantsChannel: ConflatedBroadcastChannel<List<EventParticipantItem>> = ConflatedBroadcastChannel()
    private val attendedUserIds = ConcurrentSkipListSet<String>()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            eventChannel.send(event)
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                if (it != null) {
                    eventChannel.send(it)
                } else {
                    //TODO - do nothing here or close screen
                }
                loadTickets()
            }
        }
        setupScope.launchWorker {
            eventsInteractor.getAttendedUsersChannel(eventId).consumeEach {
                attendedUserIds.clear()
                attendedUserIds.addAll(it.mapNotNull { it.takeIf { it.isPresent }?.user?.id })
                loadTickets()
            }
        }
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
        loadTicketsJob = launchWorker {
            eventsInteractor.getTicketsChannel(eventId).consumeEach {
                withContext(Dispatchers.Default) {
                    var hasConnections = false
                    var hasOtherUsers = false
                    val participants = it.mapNotNullTo(ArrayList<EventParticipantItem>(it.size)) {
                        convertTicketToParticipant(it)?.also {
                            hasConnections = hasConnections || it.isInConnections
                            hasOtherUsers = hasOtherUsers || !it.isInConnections
                        }
                    }
                    val event = eventChannel.consume { receive() }
                    if (hasConnections) {                                               //TODO: cannot load event within notifications
                        participants += EventParticipantItem.ConnectionsHeader(event.canMarkParticipants)
                    }
                    if (hasOtherUsers) {
                        participants += EventParticipantItem.OtherHeader(!hasConnections && event.canMarkParticipants)
                    }
                    participantsChannel.send(participants)
                }
            }
        }
    }

    private suspend fun convertTicketToParticipant(ticket: EventTicketModel): EventParticipantItem.User? {
        val user = userProfileInteractor.getProfileById(ticket.ownerId) ?: return null
        val isConnections = connectionsInteractor.getConnectionStatusById(ticket.ownerId) == ConnectionStatus.CONNECTED
        return EventParticipantItem.User(user, isConnections, maxOf(ticket.ticketCount.toInt() - 1, 0), isChecked = attendedUserIds.contains(user.id))
    }

}