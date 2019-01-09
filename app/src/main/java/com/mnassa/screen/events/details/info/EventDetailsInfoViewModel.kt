package com.mnassa.screen.events.details.info

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 4/18/2018.
 */
interface EventDetailsInfoViewModel : MnassaViewModel {
    val eventChannel: ConflatedBroadcastChannel<EventModel>
    val boughtTicketsChannel: ConflatedBroadcastChannel<List<EventTicketModel>>
    suspend fun loadTags(tags: List<String>): List<TagModel>
    fun buyTickets(count: Long)
}