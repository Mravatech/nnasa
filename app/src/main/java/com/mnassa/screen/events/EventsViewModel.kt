package com.mnassa.screen.events

import com.mnassa.domain.aggregator.AggregatorLive
import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.widget.newpanel.NewPanelViewModel
import kotlinx.coroutines.channels.BroadcastChannel
import java.util.*

/**
 * Created by Peter on 3/6/2018.
 */
interface EventsViewModel : MnassaViewModel, NewPanelViewModel {
    val eventsLive: AggregatorLive<EventModel>

    fun onAttachedToWindow(event: EventModel)
    fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int)

    fun saveScrollPosition(event: EventModel)
    fun restoreScrollPosition(): String?
    fun resetScrollPosition()
}