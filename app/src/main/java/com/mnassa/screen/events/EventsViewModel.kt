package com.mnassa.screen.events

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface EventsViewModel : MnassaViewModel {
    val eventsFeedChannel: BroadcastChannel<ListItemEvent<EventModel>>
    fun onAttachedToWindow(event: EventModel)
    fun resetCounter()
}