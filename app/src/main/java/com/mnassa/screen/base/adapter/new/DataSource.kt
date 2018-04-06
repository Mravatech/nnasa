package com.mnassa.screen.base.adapter.new

import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.util.SortedList
import java.lang.ref.WeakReference

/**
 * Created by Peter on 4/6/2018.
 */
interface DataSource : Iterable<AdapterItem> {
    val size: Int
    operator fun get(index: Int): AdapterItem

    override fun iterator(): Iterator<AdapterItem> {
        return object : Iterator<AdapterItem> {
            val limit = size
            var cursor = 0

            override fun hasNext(): Boolean = cursor < limit
            override fun next(): AdapterItem = get(cursor++)
        }
    }
}

interface PersistanceDataStorage: DataSource {
    fun saveState(): Bundle
    fun restoreState(state: Bundle)
}

val DataSource.isEmpty: Boolean get() = size == 0
val DataSource.isNotEmpty: Boolean get() = !this.isEmpty

interface MutableDataSource : DataSource {
    fun add(item: AdapterItem)
    fun addAll(items: Collection<AdapterItem>)
    fun remove(item: AdapterItem)
    fun remove(index: Int)
    fun removeAll(items: Collection<AdapterItem>)
    fun replace(item: AdapterItem, index: Int)
    fun replaceAll(items: List<AdapterItem>)
    fun clear()
}

class SimpleDataSource(private val updateCallback: ListUpdateCallback) : MutableDataSource {
    private val data = ArrayList<AdapterItem>()

    override val size: Int get() = data.size
    override fun get(index: Int): AdapterItem = data[index]
    override fun add(item: AdapterItem) = postUpdate {
        data += item
        updateCallback.onInserted(data.size - 1, 1)
    }

    override fun addAll(items: Collection<AdapterItem>) = postUpdate {
        data += items
        updateCallback.onInserted(data.size - items.size, items.size)
    }

    override fun remove(item: AdapterItem) = postUpdate { removeAt(data.indexOf(item)) }
    override fun remove(index: Int) = postUpdate { removeAt(index) }
    override fun removeAll(items: Collection<AdapterItem>) = postUpdate {
        items.forEach { itemToRemove ->
            removeAt(data.indexOf(itemToRemove))
        }
    }

    override fun replace(item: AdapterItem, index: Int) = postUpdate {
        val oldItem = data.set(index, item)
        if (!oldItem.isItemTheSame(item) || !oldItem.isContentTheSame(item)) {
            updateCallback.onChanged(index, 1, null)
        }
    }

    override fun replaceAll(items: List<AdapterItem>) {
        postUpdate {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = data[oldItemPosition].isItemTheSame(items[newItemPosition])
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = data[oldItemPosition].isContentTheSame(items[newItemPosition])
                override fun getOldListSize(): Int = data.size
                override fun getNewListSize(): Int = items.size
            }, true)
            data.clear()
            data.addAll(items)
            diff.dispatchUpdatesTo(updateCallback)
        }
    }

    override fun clear() {
        postUpdate {
            val oldSize = data.size
            data.clear()
            if (oldSize > 0) {
                updateCallback.onRemoved(0, oldSize)
            }
        }
    }

    private fun removeAt(index: Int) {
        if (index in 0..(size - 1)) {
            data.removeAt(index)
            updateCallback.onRemoved(index, 1)
        }
    }

    private inline fun postUpdate(crossinline update: () -> Unit) = update()
}

class SortedDataSource(private val updateCallback: ListUpdateCallback) : MutableDataSource {
    private val callback = object : SortedList.Callback<AdapterItem>() {
        override fun areItemsTheSame(item1: AdapterItem, item2: AdapterItem): Boolean = item1.isItemTheSame(item2)
        override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean = oldItem.isContentTheSame(newItem)
        override fun onMoved(fromPosition: Int, toPosition: Int) = updateCallback.onMoved(fromPosition, toPosition)
        override fun onChanged(position: Int, count: Int) = updateCallback.onChanged(position, count, null)
        override fun onInserted(position: Int, count: Int) = updateCallback.onInserted(position, count)
        override fun onRemoved(position: Int, count: Int) = updateCallback.onRemoved(position, count)
        override fun compare(o1: AdapterItem, o2: AdapterItem): Int = o1.compareTo(o2)
    }
    private val sortedList = SortedList<AdapterItem>(AdapterItem::class.java, callback)
    override val size: Int get() = sortedList.size()
    override fun get(index: Int): AdapterItem = sortedList[index]

    override fun add(item: AdapterItem) = postUpdate { sortedList.add(item) }
    override fun addAll(items: Collection<AdapterItem>) = postUpdate { sortedList.addAll(items) }
    override fun remove(item: AdapterItem) = postUpdate { sortedList.remove(item) }
    override fun remove(index: Int) = postUpdate { sortedList.removeItemAt(index) }
    override fun removeAll(items: Collection<AdapterItem>) = postUpdate {
        items.forEach {
            sortedList.remove(it)
        }
    }

    override fun replace(item: AdapterItem, index: Int) = postUpdate {
        sortedList.updateItemAt(index, item)
    }

    override fun replaceAll(items: List<AdapterItem>) = postUpdate { sortedList.replaceAll(items) }
    override fun clear() = postUpdate { sortedList.clear() }
    private inline fun postUpdate(crossinline update: () -> Unit) = update()
}

class AsyncDataSourceWrapper(
        private val dataSource: MutableDataSource,
        private val eventsQueue: (() -> Unit) -> Unit) : MutableDataSource {
    private fun postUpdate(update: () -> Unit) = eventsQueue(update)
    override fun add(item: AdapterItem) = postUpdate { dataSource.add(item) }
    override fun addAll(items: Collection<AdapterItem>) = postUpdate { dataSource.addAll(items) }
    override fun remove(item: AdapterItem) = postUpdate { dataSource.remove(item) }
    override fun remove(index: Int) = postUpdate { dataSource.remove(index) }
    override fun removeAll(items: Collection<AdapterItem>) = postUpdate { dataSource.removeAll(items) }
    override fun replace(item: AdapterItem, index: Int) = postUpdate { dataSource.replace(item, index) }
    override fun replaceAll(items: List<AdapterItem>) = postUpdate { dataSource.replaceAll(items) }
    override fun clear() = postUpdate { dataSource.clear() }
    override val size: Int get() = dataSource.size
    override fun get(index: Int): AdapterItem = dataSource[index]
}

class ConnectableListUpdateCallbackWrapper(var delegate: WeakReference<ListUpdateCallback?>) : ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) = connectedDataSource.onChanged(position, count, payload)
    override fun onMoved(fromPosition: Int, toPosition: Int) = connectedDataSource.onMoved(fromPosition, toPosition)
    override fun onInserted(position: Int, count: Int) = connectedDataSource.onInserted(position, count)
    override fun onRemoved(position: Int, count: Int) = connectedDataSource.onRemoved(position, count)

    private val connectedDataSource: ListUpdateCallback
        get() = requireNotNull(delegate.get()) {
            "Connected data source is required!"
        }
}
