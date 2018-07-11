package com.mnassa.screen.base.adapter

import android.support.v7.util.SortedList
import android.support.v7.widget.util.SortedListAdapterCallback

/**
 * Created by Peter on 3/7/2018.
 */
abstract class BaseSortedPaginationRVAdapter<ITEM>(reverseOrder: Boolean = false) : BasePaginationRVAdapter<ITEM>(reverseOrder) {
    abstract val itemsComparator: (item1: ITEM, item2: ITEM) -> Int
    abstract val itemClass: Class<ITEM>

    open class SortedDataStorage<ITEM>(itemClass: Class<ITEM>, private val adapter: BaseSortedPaginationRVAdapter<ITEM>) : DataStorage<ITEM> {
        val wrappedList = SortedList<ITEM>(itemClass, SortedDataStorageCallback(adapter))

        override fun clear() {
            adapter.postDataUpdate {
                wrappedList.beginBatchedUpdates()
                wrappedList.clear()
                wrappedList.endBatchedUpdates()
            }
        }

        override fun add(element: ITEM): Boolean {
            adapter.postDataUpdate {
                wrappedList.beginBatchedUpdates()
                wrappedList.add(element)
                wrappedList.endBatchedUpdates()
            }

            return true
        }

        override fun addAll(elements: Collection<ITEM>): Boolean {
            adapter.postDataUpdate {
                wrappedList.beginBatchedUpdates()
                wrappedList.addAll(elements)
                wrappedList.endBatchedUpdates()
            }
            return true
        }

        override fun remove(element: ITEM): Boolean {
            adapter.postDataUpdate {
                wrappedList.beginBatchedUpdates()
                wrappedList.remove(element)
                wrappedList.endBatchedUpdates()
            }
            return true
        }

        override fun set(elements: List<ITEM>) {
            adapter.postDataUpdate {
                wrappedList.beginBatchedUpdates()
                wrappedList.replaceAll(elements)
                wrappedList.endBatchedUpdates()
            }
        }

        override fun removeAll(elements: Collection<ITEM>): Boolean {
            adapter.postDataUpdate {
                wrappedList.beginBatchedUpdates()
                elements.forEach {
                    wrappedList.remove(it)
                }
                wrappedList.endBatchedUpdates()
            }
            return true
        }

        override fun get(index: Int): ITEM = wrappedList[index]

        override val size: Int get() = wrappedList.size()

        override fun iterator(): Iterator<ITEM> {
            return object : Iterator<ITEM> {
                private var cursor: Int = 0
                override fun hasNext(): Boolean = cursor < size
                override fun next(): ITEM = get(cursor++)
            }
        }
    }

    private class SortedDataStorageCallback<ITEM>(private val adapter: BaseSortedPaginationRVAdapter<ITEM>) : SortedList.Callback<ITEM>() {
        private val defCallback = object : SortedListAdapterCallback<ITEM>(adapter) {
            override fun areItemsTheSame(item1: ITEM, item2: ITEM): Boolean = adapter.itemsTheSameComparator(item1, item2)
            override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = adapter.contentTheSameComparator(oldItem, newItem)
            override fun compare(o1: ITEM, o2: ITEM): Int = adapter.itemsComparator(o1, o2)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) = defCallback.onMoved(convert(fromPosition), convert(toPosition))
        override fun onRemoved(position: Int, count: Int) = defCallback.onRemoved(convert(position), count)
        override fun onChanged(position: Int, count: Int) = defCallback.onChanged(convert(position), count)
        override fun onInserted(position: Int, count: Int) = defCallback.onInserted(convert(position), count)

        override fun compare(o1: ITEM, o2: ITEM): Int = defCallback.compare(o1, o2)
        override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = defCallback.areContentsTheSame(oldItem, newItem)
        override fun areItemsTheSame(item1: ITEM, item2: ITEM): Boolean = defCallback.areItemsTheSame(item1, item2)

        private fun convert(dataPosition: Int) = adapter.convertDataIndexToAdapterPosition(dataPosition)
    }
}