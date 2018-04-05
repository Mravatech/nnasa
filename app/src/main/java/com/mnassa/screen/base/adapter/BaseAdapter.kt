package com.mnassa.screen.base.adapter

import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.haha.guava.collect.Iterators

/**
 * Created by Peter on 4/5/2018.
 */
interface BaseAdapter {
    val dataStorage: MutableDataSource
    val listUpdateCallback: ListUpdateCallback
}

open class XAdapter : RecyclerView.Adapter<XAdapter.ViewHolder>(), BaseAdapter {
    private val viewTypesMap = SparseArray<AdapterItemViewType>()
    private lateinit var layoutInflater: LayoutInflater

    override val listUpdateCallback: ListUpdateCallback = AdapterListUpdateCallback(this)
    override val dataStorage: MutableDataSource = TODO()

    override fun getItemCount(): Int = dataStorage.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = dataStorage[position].bind(holder.itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (this::layoutInflater.isInitialized) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        lateinit var holder: ViewHolder
        val onClickListener = View.OnClickListener { view ->
            val position = holder.adapterPosition
            if (position >= 0) {
                onClick(view, position)
            }
        }

        val view = viewTypesMap.get(viewType).inflate(parent, layoutInflater, onClickListener)
        holder = ViewHolder(view)
        return holder
    }

    /**
     * Returns true, if event has intercepted
     */
    open fun onClick(view: View, adapterPosition: Int): Boolean = false

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

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

interface MutableDataSource : DataSource {
    fun add(item: AdapterItem)
    fun addAll(items: Collection<AdapterItem>)
    //
    fun remove(item: AdapterItem)
    fun remove(index: Int)

    fun removeAll(items: Collection<AdapterItem>)
    //
    fun set(items: List<AdapterItem>)

    //
    fun clear()
}

class SimpleDataSource(updateCallback: () -> ListUpdateCallback,
                       private val eventsQueue: (() -> Unit) -> Unit): MutableDataSource {

    private val data = ArrayList<AdapterItem>()
    private val updateCallback by lazy(updateCallback)

    private fun postUpdate(update: (() -> Unit)) {
        eventsQueue(update)
    }

    override val size: Int get() = data.size

    override fun get(index: Int): AdapterItem = data[index]

    override fun add(item: AdapterItem) {
        postUpdate {
            data += item
            updateCallback.onInserted(data.size - 1, 1)
        }
    }

    override fun addAll(items: Collection<AdapterItem>) {
        postUpdate {
            data += items
            updateCallback.onInserted(data.size - items.size, items.size)
        }
    }

    override fun remove(item: AdapterItem) {
        postUpdate { removeAt(data.indexOf(item)) }
    }

    override fun remove(index: Int) {
        postUpdate { removeAt(index) }
    }

    override fun removeAll(items: Collection<AdapterItem>) {
        postUpdate {
            items.forEach { itemToRemove ->
                removeAt(data.indexOf(itemToRemove))
            }
        }
    }

    override fun set(items: List<AdapterItem>) {
        postUpdate {
            val diff = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
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
        if (index >= 0 && index < data.size) {
            data.removeAt(index)
            updateCallback.onRemoved(index, 1)
        }
    }
}


interface AdapterItemViewType {
    fun inflate(parent: ViewGroup, layoutInflater: LayoutInflater, onClickListener: View.OnClickListener): View
    fun bind(view: View, item: AdapterItem)
}

interface AdapterItem : Comparable<AdapterItem> {
    val viewType: AdapterItemViewType

    fun isContentTheSame(other: AdapterItem): Boolean
    fun isItemTheSame(other: AdapterItem): Boolean
    override fun compareTo(other: AdapterItem): Int
    fun bind(view: View) = viewType.bind(view, this)
}
