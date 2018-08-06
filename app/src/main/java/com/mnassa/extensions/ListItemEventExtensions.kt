package com.mnassa.extensions

import android.view.View
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 8/6/2018.
 */
suspend fun <T : Any> ReceiveChannel<ListItemEvent<List<T>>>.subscribeToUpdates(
        adapter: BasePaginationRVAdapter<T>,
        emptyView: suspend () -> View? = { null },
        onAdded: () -> Unit = {},
        onCleared: () -> Unit = {}) {
    adapter.isLoadingEnabled = adapter.dataStorage.isEmpty()

    consumeEach {
        when (it) {
            is ListItemEvent.Added -> {
                adapter.isLoadingEnabled = false
                adapter.dataStorage.addAll(it.item)
                emptyView()?.isInvisible = !adapter.dataStorage.isEmpty()
                onAdded()
            }
            is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
            is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
            is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
            is ListItemEvent.Cleared -> {
                adapter.isLoadingEnabled = true
                adapter.dataStorage.clear()
                onCleared()
            }
        }
    }
}

suspend fun <T : Any> BroadcastChannel<ListItemEvent<List<T>>>.subscribeToUpdates(
        adapter: BasePaginationRVAdapter<T>,
        emptyView: suspend () -> View? = { null },
        onAdded: () -> Unit = {},
        onCleared: () -> Unit = {}
) {
    openSubscription().subscribeToUpdates(adapter, emptyView, onAdded, onCleared)
}