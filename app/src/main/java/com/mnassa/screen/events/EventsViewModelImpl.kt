package com.mnassa.screen.events

import android.os.Bundle
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.repository.EventsRepository
import com.mnassa.extensions.ReConsumeWhenAccountChangedArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 3/6/2018.
 */
class EventsViewModelImpl(private val eventsRepository: EventsRepository) : MnassaViewModelImpl(), EventsViewModel {

    override val eventsFeedChannel: BroadcastChannel<ListItemEvent<EventModel>>by ReConsumeWhenAccountChangedArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { eventsRepository.getEventsFeedChannel() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            eventsRepository.getEventsFeedChannel().consumeEach {
                Timber.e("EVENT >>> ${it.item}")
            }
        }
    }

    override fun onAttachedToWindow(event: EventModel) {

    }
}