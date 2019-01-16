package com.mnassa.screen.events

import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.PreferencesInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.delay
import java.util.*
import java.util.Collections.min
import kotlin.math.min

/**
 * Created by Peter on 3/6/2018.
 */
class EventsViewModelImpl(private val eventsInteractor: EventsInteractor, private val preferencesInteractor: PreferencesInteractor) : MnassaViewModelImpl(), EventsViewModel {

    private var isCounterReset = false
    private var resetCounterJob: Job? = null

    override val eventsFeedChannel: BroadcastChannel<ListItemEvent<List<EventModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = {
                isCounterReset = false
                it.send(ListItemEvent.Cleared())
            },
            receiveChannelProvider = {
                eventsInteractor.getEventsFeedChannel()
            })

    override fun onAttachedToWindow(event: EventModel) {
        handleException { eventsInteractor.onItemViewed(event) }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = async {
            delay(1_000)
            resetCounter()
        }
    }

    override fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int) {
        val paginationController = eventsInteractor.eventsPagination
        val paginationSize = min(paginationController.size, totalItemCount.toLong())
        if (visibleItemCount + firstVisibleItemPosition >= paginationSize && firstVisibleItemPosition >= 0) {
            paginationController.requestNextPage(100)
        }
    }

    private fun resetCounter() {
        handleException {
            try {
                eventsInteractor.resetCounter()
                isCounterReset = true
            } catch (e: NetworkException) {
                //ignore
            }
        }
    }

    override fun saveScrollPosition(event: EventModel) {
        preferencesInteractor.saveString(KEY_EVENTS_POSITION, event.id)
    }

    override fun restoreScrollPosition(): String? = preferencesInteractor.getString(KEY_EVENTS_POSITION)

    override fun resetScrollPosition() {
        preferencesInteractor.saveString(KEY_EVENTS_POSITION, null)
    }

    override fun getLastViewedEventDate(): Date? {
        return preferencesInteractor.getLong(KEY_EVENTS_LAST_VIEWED, -1).takeIf { it >= 0 }?.let { Date(it) }
    }

    override fun setLastViewedEventDate(date: Date?) {
        preferencesInteractor.saveLong(KEY_EVENTS_LAST_VIEWED, date?.time ?: -1)
    }

    private suspend fun getAllEvents() = handleExceptionsSuspend { eventsInteractor.loadAllImmediately() }
            ?: emptyList()

    private companion object {
        private const val KEY_EVENTS_POSITION = "KEY_EVENTS_POSITION"
        private const val KEY_EVENTS_LAST_VIEWED = "KEY_EVENTS_LAST_VIEWED"
    }
}