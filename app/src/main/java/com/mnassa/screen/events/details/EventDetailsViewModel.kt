package com.mnassa.screen.events.details

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventStatus
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/17/2018.
 */
interface EventDetailsViewModel : MnassaViewModel {
    val eventChannel: BroadcastChannel<EventModel>
    val finishScreenChannel: BroadcastChannel<Unit>

    fun changeStatus(event: EventModel, status: EventStatus)
    fun sendComplaint(eventId: String, reason: String, authorText: String?)
    suspend fun retrieveComplaints(): List<TranslatedWordModel>
}