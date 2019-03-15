package com.mnassa.extensions

import android.view.View
import com.mnassa.domain.aggregator.AggregatorOutEvent
import com.mnassa.domain.aggregator.AggregatorOutState
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 8/6/2018.
 */
suspend fun <T : HasId> ReceiveChannel<AggregatorOutState<out T>>.subscribeToUpdates(
        adapter: BasePaginationRVAdapter<T>,
        emptyView: suspend () -> View? = { null },
        onAdded: () -> Unit = {},
        onCleared: () -> Unit = {}) {
    adapter.isLoadingEnabled = adapter.dataStorage.isEmpty()

    consumeEach { state ->
        val event = state.event

        val shouldLoadingBeEnabled = when (event) {
            is AggregatorOutEvent.Clear -> true
            is AggregatorOutEvent.Reset -> adapter.isLoadingEnabled && state.models.isEmpty()
            else -> false
        }

        emptyView()?.isInvisible = state.models.isNotEmpty() && !shouldLoadingBeEnabled
        adapter.isLoadingEnabled = shouldLoadingBeEnabled

        when (event) {
            is AggregatorOutEvent.Clear -> {
                adapter.dataStorage.clear()
                onCleared()
            }
            is AggregatorOutEvent.Reset -> {
                adapter.dataStorage.addAll(state.models)
                onAdded()
            }
            is AggregatorOutEvent.Add -> {
                adapter.dataStorage.add(event.model as T)
                onAdded()
            }
            is AggregatorOutEvent.Set -> {
                adapter.dataStorage.add(event.model as T)
            }
            is AggregatorOutEvent.Remove -> {
                val model = event.model as T
                adapter.dataStorage.firstOrNull { model.id == it.id }
                    ?.also {
                        adapter.dataStorage.remove(it)
                    }
            }
        }
    }
}
