package com.mnassa.screen.events.details.info

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsInfoViewModelImpl(
        private val eventId: String,
        private val eventsInteractor: EventsInteractor,
        private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), EventDetailsInfoViewModel {
    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()
    private val ticketsChannel: ConflatedBroadcastChannel<List<EventTicketModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                it?.apply { eventChannel.send(this) }
            }
        }

        handleException {
            eventsInteractor.getTicketsChannel(eventId).consumeEach {
                ticketsChannel.send(it)
                eventChannel.valueOrNull?.apply { eventChannel.send(this) }
            }
        }
    }

    override suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { tag -> asyncWorker { tagInteractor.get(tag) } }.mapNotNull { it.await() }
    }

    override fun buyTickets(count: Long) {
        Timber.e("Buy tickets: $count")
        if (count <= 0) return

        handleException {
            withProgressSuspend {
                eventsInteractor.buyTickets(eventId, count)
            }
        }

    }
}