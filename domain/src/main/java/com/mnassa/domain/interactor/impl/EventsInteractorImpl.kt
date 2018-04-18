package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.bufferize
import com.mnassa.domain.repository.EventsRepository
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

/**
 * Created by Peter on 4/13/2018.
 */
class EventsInteractorImpl(private val eventsRepository: EventsRepository, private val userProfileInteractor: UserProfileInteractor) : EventsInteractor {
    private val viewItemChannel = ArrayChannel<ListItemEvent<EventModel>>(10)

    init {
        launch {
            viewItemChannel.bufferize(SubscriptionsContainerDelegate(), SEND_VIEWED_ITEMS_BUFFER_DELAY).consumeEach {
                if (it.item.isNotEmpty()) {
                    try {
                        eventsRepository.sendViewed(it.item.map { it.id })
                    } catch (e: Exception) {
                        Timber.d(e)
                    }
                }
            }
        }
    }

    override suspend fun onItemViewed(item: EventModel) {
        if (item.author.id == userProfileInteractor.getAccountIdOrException()) {
            return
        }
        viewItemChannel.send(ListItemEvent.Added(item))
    }

    override suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>> {
        return eventsRepository.getEventsFeedChannel()
    }

    override suspend fun loadByIdChannel(eventId: String): ReceiveChannel<EventModel?> {
        return eventsRepository.getEventsChannel(eventId)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}