package com.mnassa.screen.events.details.info

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventTicketModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsInfoViewModelImpl(
        private val eventId: String,
        private val eventsInteractor: EventsInteractor,
        private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), EventDetailsInfoViewModel {

    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()
    override val boughtTicketsChannel: ConflatedBroadcastChannel<List<EventTicketModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                it?.apply { eventChannel.send(this) }
            }
        }
        setupScope.launchWorker {
            eventsInteractor.getBoughtTicketsChannel(eventId).consumeEach {
                boughtTicketsChannel.send(it)
            }
        }
    }

    override suspend fun loadTags(tags: List<String>): List<TagModel> = handleExceptionsSuspend { tagInteractor.get(tags) } ?: emptyList()

    override fun buyTickets(count: Long) {
        Timber.e("Buy tickets: $count")
        if (count <= 0) return

        launchWorker {
            withProgressSuspend {
                eventsInteractor.buyTickets(eventId, count)
            }
        }

    }
}