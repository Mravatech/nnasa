package com.mnassa.screen.events

import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class EventsViewModelImpl(private val eventsInteractor: EventsInteractor) : MnassaViewModelImpl(), EventsViewModel {

    override val eventsFeedChannel: BroadcastChannel<ListItemEvent<EventModel>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { eventsInteractor.getEventsFeedChannel() })

    override fun onAttachedToWindow(event: EventModel) {
        handleException {
            eventsInteractor.onItemViewed(event)
        }
    }

    override fun resetCounter() {
        handleException {
            eventsInteractor.resetCounter()
        }
    }
}