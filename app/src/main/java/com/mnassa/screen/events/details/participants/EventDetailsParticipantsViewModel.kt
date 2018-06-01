package com.mnassa.screen.events.details.participants

import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/18/2018.
 */
interface EventDetailsParticipantsViewModel : MnassaViewModel {
    val eventChannel: BroadcastChannel<EventModel>
    val participantsChannel: BroadcastChannel<List<EventParticipantItem>>

    suspend fun saveParticipants(participants: List<EventParticipantItem>)
}