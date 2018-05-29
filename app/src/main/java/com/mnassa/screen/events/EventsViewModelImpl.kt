package com.mnassa.screen.events

import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 3/6/2018.
 */
class EventsViewModelImpl(private val eventsInteractor: EventsInteractor) : MnassaViewModelImpl(), EventsViewModel {

    private var isCounterReset = false
    private var resetCounterJob: Job? = null

    override val eventsFeedChannel: BroadcastChannel<ListItemEvent<EventModel>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = {
                isCounterReset = false
                it.send(ListItemEvent.Cleared())
            },
            receiveChannelProvider = { eventsInteractor.getEventsFeedChannel() })


    override fun onAttachedToWindow(event: EventModel) {
        handleException { eventsInteractor.onItemViewed(event) }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = async {
            delay(1_000)
            resetCounter()
        }
    }

    private fun resetCounter() {
        handleException {
            eventsInteractor.resetCounter()
            isCounterReset = true
        }
    }
}