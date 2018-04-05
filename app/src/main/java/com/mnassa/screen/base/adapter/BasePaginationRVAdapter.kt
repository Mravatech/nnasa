package com.mnassa.screen.base.adapter

/**
 * Created by Peter on 3/7/2018.
 */
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_loading.view.*
import java.lang.ref.WeakReference

abstract class BasePaginationRVAdapter<ITEM> : RecyclerView.Adapter<BasePaginationRVAdapter.BaseVH<ITEM>>() {
    protected var recyclerView = StateExecutor(
            initState = WeakReference(null as RecyclerView?),
            executionPredicate = { it.get() != null },
            transformer = { requireNotNull(it.get()) })

    protected inline fun postUpdate(crossinline update: (() -> Unit)) {
        recyclerView.invoke {
            it.post { update() }
        }
    }
    var itemsTheSameComparator: ((item1: ITEM, item2: ITEM) -> Boolean) = { item1, item2 -> item1 == item2 }
    var contentTheSameComparator: ((oldItem: ITEM, newItem: ITEM) -> Boolean) = { _, _ -> true }
    var dataStorage: DataStorage<ITEM> = SimpleDataProviderImpl()
        set(value) {
            field = value
            postUpdate { notifyDataSetChanged() }
        }

    var isLoadingEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                postUpdate { notifyItemChanged(itemCount - 1) }
            }
        }

    /////////////////////////////////// BASIC DATASET OPERATIONS ///////////////////////////////////
    open fun clear() = run { dataStorage.clear() }
    open fun set(list: List<ITEM>) = dataStorage.set(list)
    open fun add(list: List<ITEM>) = run { dataStorage.addAll(list); Unit }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onAttachedToRecyclerView(rv: RecyclerView) = run { recyclerView.value = WeakReference(rv) }
    override fun onDetachedFromRecyclerView(rv: RecyclerView) = run {
        recyclerView.value.clear()
        recyclerView.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<ITEM> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_LOADING_ENABLED -> {
                val view = inflater.inflate(R.layout.item_loading, parent, false)
                view.tvLoading.text = fromDictionary(R.string.loading)
                LoadingViewHolder(view)
            }
            TYPE_LOADING_DISABLED, TYPE_HEADER -> LoadingViewHolder(View(parent.context))
            else -> onCreateViewHolder(parent, viewType, inflater)
        }
    }

    //////////////////////////////////// VIEW HOLDER CREATION //////////////////////////////////////

    /**
     * Create view here instead of onCreateViewHolder
     */
    protected abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ITEM>

    override fun onBindViewHolder(holder: BaseVH<ITEM>, position: Int) {
        when (getItemViewType(position)) {
            TYPE_LOADING_ENABLED, TYPE_LOADING_DISABLED, TYPE_HEADER -> {
            }
            else -> holder.bind(getDataItemByAdapterPosition(position))
        }
    }

    //////////////////////////////////////// VIEW TYPES ////////////////////////////////////////////

    open fun getViewType(position: Int): Int = TYPE_UNDEFINED
    final override fun getItemViewType(position: Int) = when (position) {
        0 -> TYPE_HEADER
        itemCount - 1 -> if (isLoadingEnabled) TYPE_LOADING_ENABLED else TYPE_LOADING_DISABLED
        else -> getViewType(position - emptyHeaderItemsCount)
    }

    /////////////////////////////////// ITEMS COUNT & POSITIONS ////////////////////////////////////

    override fun getItemCount(): Int = dataStorage.size + emptyItemCount
    open val emptyItemCount: Int get() = emptyHeaderItemsCount + emptyBottomItemsCount
    private val emptyHeaderItemsCount = 1
    private val emptyBottomItemsCount = 1
    fun getDataItemByAdapterPosition(position: Int): ITEM = dataStorage[position - emptyHeaderItemsCount]
    fun convertDataIndexToAdapterPosition(index: Int): Int = index + emptyHeaderItemsCount
    fun convertAdapterPositionToDataIndex(index: Int): Int {
        return when {
            index < 0 -> index
            index == 0 -> -1
            index >= (itemCount - 1) -> -1
            else -> index - emptyHeaderItemsCount
        }
    }

    /////////////////////////////////// ABSTRACT VIEW HOLDERS //////////////////////////////////////

    abstract class BaseVH<in ITEM>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: ITEM)
    }

    private class LoadingViewHolder<in ITEM>(itemView: View) : BaseVH<ITEM>(itemView) {
        override fun bind(item: ITEM) {}
    }

    ////////////////////////////////////// ABSTRACT DATA STORAGE ///////////////////////////////////

    interface DataStorage<T> : Iterable<T> {
        fun clear()
        fun add(element: T): Boolean
        fun addAll(elements: Collection<T>): Boolean
        fun remove(element: T): Boolean
        fun removeAll(elements: Collection<T>): Boolean
        fun set(elements: List<T>)
        operator fun get(index: Int): T
        val size: Int
    }

    ////////////////////////////////////////// DATA STORAGE ////////////////////////////////////////

    open inner class SimpleDataProviderImpl(private val onDataChangedListener: () -> Unit = { }) : ArrayList<ITEM>(), DataStorage<ITEM> {
        override fun clear() {
            postUpdate {
                val previousSize = size
                super.clear()
                if (previousSize != 0) notifyItemRangeRemoved(emptyHeaderItemsCount, previousSize)
                onDataChangedListener()
            }
        }

        override fun add(element: ITEM): Boolean {
            postUpdate {
                super.add(element)
                notifyItemInserted(itemCount - emptyBottomItemsCount - 1)
                onDataChangedListener()
            }
            return true
        }

        override fun addAll(elements: Collection<ITEM>): Boolean {
            postUpdate {
                val newDataList = ArrayList(this)
                newDataList.addAll(elements)

                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(newDataList)), true)
                super.clear()
                super.addAll(newDataList)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
                onDataChangedListener()
            }
            return true
        }

        override fun set(elements: List<ITEM>) {
            postUpdate {
                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(elements)), true)
                super.clear()
                super.addAll(elements)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
                onDataChangedListener()
            }
        }

        override fun remove(element: ITEM): Boolean {
            postUpdate {
                val newDataList = ArrayList(this)
                newDataList.remove(element)

                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(newDataList)), true)
                super.clear()
                super.addAll(newDataList)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
                onDataChangedListener()
            }
            return true
        }

        override fun removeAll(elements: Collection<ITEM>): Boolean {
            postUpdate {
                val newDataList = ArrayList(this)
                newDataList.removeAll(elements)

                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(newDataList)), true)
                super.clear()
                super.addAll(newDataList)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
                onDataChangedListener()
            }
            return true
        }
    }

    /////////////////////////////////////// DIFF UTILS HELPERS /////////////////////////////////////

    inner class DiffUtilsCallback(private val oldData: DataStorage<ITEM>, private val newData: DataStorage<ITEM>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItemType: Int = when (oldItemPosition) {
                0 -> TYPE_HEADER
                oldListSize - 1 -> TYPE_LOADING_ENABLED
                else -> TYPE_UNDEFINED
            }
            val newItemType: Int = when (newItemPosition) {
                0 -> TYPE_HEADER
                newListSize - 1 -> TYPE_LOADING_ENABLED
                else -> TYPE_UNDEFINED
            }

            return if (oldItemType == TYPE_UNDEFINED && newItemType == TYPE_UNDEFINED) {
                val oldClientPos = oldItemPosition - emptyHeaderItemsCount
                val newClientPos = newItemPosition - emptyHeaderItemsCount
                val oldClientItem = oldData[oldClientPos]
                val newClientItem = newData[newClientPos]

                return itemsTheSameComparator.invoke(oldClientItem, newClientItem)
            } else {
                oldItemType == newItemType
            }
        }

        override fun getOldListSize(): Int = oldData.size + emptyItemCount
        override fun getNewListSize(): Int = newData.size + emptyItemCount
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItemType: Int = when (oldItemPosition) {
                0 -> TYPE_HEADER
                oldListSize - 1 -> TYPE_LOADING_ENABLED
                else -> TYPE_UNDEFINED
            }
            val newItemType: Int = when (newItemPosition) {
                0 -> TYPE_HEADER
                newListSize - 1 -> TYPE_LOADING_ENABLED
                else -> TYPE_UNDEFINED
            }

            return if (oldItemType == TYPE_UNDEFINED && newItemType == TYPE_UNDEFINED) {

                val oldClientPos = oldItemPosition - emptyHeaderItemsCount
                val newClientPos = newItemPosition - emptyHeaderItemsCount
                val oldClientItem = oldData[oldClientPos]
                val newClientItem = newData[newClientPos]

                return contentTheSameComparator.invoke(oldClientItem, newClientItem)
            } else oldItemType == newItemType
        }
    }

    class MutableDataStorageWrapper<T>(private val wrappedMutableVal: MutableList<T>) : ReadOnlyDataStorageWrapper<T>(wrappedMutableVal) {
        override fun clear(): Unit = wrappedMutableVal.clear()
        override fun add(element: T): Boolean = wrappedMutableVal.add(element)
        override fun addAll(elements: Collection<T>): Boolean = wrappedMutableVal.addAll(elements)
        override fun remove(element: T): Boolean = wrappedMutableVal.remove(element)
        override fun removeAll(elements: Collection<T>): Boolean = wrappedMutableVal.removeAll(elements)
        override fun set(elements: List<T>) {
            wrappedMutableVal.clear()
            wrappedMutableVal.addAll(elements)
        }
        override fun get(index: Int): T = wrappedMutableVal[index]

        override fun iterator(): Iterator<T> {
            return object : Iterator<T> {
                private var cursor: Int = 0
                override fun hasNext(): Boolean = cursor < size

                override fun next(): T = get(cursor++)
            }
        }
    }

    open class ReadOnlyDataStorageWrapper<T>(private val wrappedVal: List<T>) : DataStorage<T> {
        override fun clear(): Unit = throw IllegalStateException("clear() called in the ReadOnlyDataStorageWrapper")
        override fun add(element: T): Boolean = throw IllegalStateException("add() called in the ReadOnlyDataStorageWrapper")
        override fun addAll(elements: Collection<T>): Boolean = throw IllegalStateException("addAll() called in the ReadOnlyDataStorageWrapper")
        override fun remove(element: T): Boolean = throw IllegalStateException("remove() called in the ReadOnlyDataStorageWrapper")
        override fun removeAll(elements: Collection<T>): Boolean = throw IllegalStateException("removeAll() called in the ReadOnlyDataStorageWrapper")

        override fun set(elements: List<T>): Unit = throw IllegalStateException("set() called in the ReadOnlyDataStorageWrapper")
        override fun get(index: Int): T = wrappedVal[index]
        override val size: Int get() = wrappedVal.size

        override fun iterator(): Iterator<T> {
            return object : Iterator<T> {
                private var cursor: Int = 0
                override fun hasNext(): Boolean = cursor < size

                override fun next(): T = get(cursor++)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        const val TYPE_UNDEFINED = -1
        const val TYPE_HEADER = 7778881
        const val TYPE_LOADING_ENABLED = 777888
        const val TYPE_LOADING_DISABLED = -888777
    }
}