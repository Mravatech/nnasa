package com.mnassa.screen.events

import com.mnassa.core.addons.launchWorker
import com.mnassa.core.addons.launchWorkerNoExceptions
import com.mnassa.domain.aggregator.AggregatorLive
import com.mnassa.domain.aggregator.produce
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.PreferencesInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.min

/**
 * Created by Peter on 3/6/2018.
 */
class EventsViewModelImpl(private val eventsInteractor: EventsInteractor, private val preferencesInteractor: PreferencesInteractor) : MnassaViewModelImpl(), EventsViewModel {

    private var resetCounterJob: Job? = null

    override val eventsLive: AggregatorLive<EventModel>
        get() = eventsInteractor.eventsLive

    override val scrollToTopChannel: BroadcastChannel<Unit> = BroadcastChannel(1)

    override val newItemsCounterChannel: BroadcastChannel<Int> = BroadcastChannel(Channel.CONFLATED)

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            eventsLive.produce().consumeEach { state ->
                newItemsCounterChannel.send(state.modelsAllDeltaCount)
            }
        }
    }

    override fun onAttachedToWindow(event: EventModel) {
        GlobalScope.launchWorkerNoExceptions {
            eventsInteractor.onItemViewed(event)
        }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = GlobalScope.launchWorkerNoExceptions {
            delay(1_000)
            resetCounter()
        }
    }

    override fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int) {
        val paginationController = eventsInteractor.eventsPagination
        val paginationSize = min(paginationController.size, totalItemCount.toLong())
        if (visibleItemCount + firstVisibleItemPosition >= paginationSize && firstVisibleItemPosition >= 0) {
            paginationController.requestNextPage(EVENTS_PAGE_SIZE)
        }
    }

    private fun resetCounter() {
        launchWorker {
            eventsInteractor.resetCounter()
        }
    }

    override fun setNewItemsTimeUpperBound(date: Date) {
        eventsInteractor.eventsLiveTimeUpperBound = date
    }

    override fun saveScrollPosition(event: EventModel) {
        preferencesInteractor.saveString(KEY_EVENTS_POSITION, event.id)
    }

    override fun restoreScrollPosition(): String? = preferencesInteractor.getString(KEY_EVENTS_POSITION)

    override fun resetScrollPosition() {
        preferencesInteractor.saveString(KEY_EVENTS_POSITION, null)
    }

    private companion object {
        private const val KEY_EVENTS_POSITION = "KEY_EVENTS_POSITION"

        private const val EVENTS_PAGE_SIZE = 60L
    }
}