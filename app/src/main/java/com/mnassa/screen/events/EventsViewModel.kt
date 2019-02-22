package com.mnassa.screen.events

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel
import java.util.*

/**
 * Created by Peter on 3/6/2018.
 */
interface EventsViewModel : MnassaViewModel {
    val eventsFeedChannel: BroadcastChannel<ListItemEvent<List<EventModel>>>
    fun onAttachedToWindow(event: EventModel)
    fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int)

    fun saveScrollPosition(event: EventModel)
    fun restoreScrollPosition(): String?
    fun resetScrollPosition()

    fun getLastViewedEventDate(): Date?
    fun setLastViewedEventDate(date: Date?)
}