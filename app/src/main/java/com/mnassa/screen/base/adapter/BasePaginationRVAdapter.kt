package com.mnassa.screen.base.adapter

/**
 * Created by Peter on 3/7/2018.
 */
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.core.addons.WeakStateExecutor
import com.mnassa.translation.fromDictionary
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_loading.view.*
import java.lang.ref.WeakReference

//TODO: use androidx.recyclerview.widget.ListAdapter & AsyncListDiffer
abstract class BasePaginationRVAdapter<ITEM>(var reverseOrder: Boolean = false) : RecyclerView.Adapter<BasePaginationRVAdapter.BaseVH<ITEM>>() {
    protected var recyclerView = WeakStateExecutor<RecyclerView?, RecyclerView>(
            initState = null,
            executionPredicate = { it != null })

    inline fun postDataUpdate(crossinline update: (() -> Unit)) {
        `access$recyclerView`.invoke {
            it.post {
                update()
                onDataChangedListener(dataStorage.size)
            }
        }
    }
    inline fun postRecyclerViewUpdate(crossinline update: (() -> Unit)) {
        `access$recyclerView`.invoke {
            it.post {
                update()
            }
        }
    }

    var itemsTheSameComparator: ((item1: ITEM, item2: ITEM) -> Boolean) = { item1, item2 -> item1 == item2 }
    var contentTheSameComparator: ((oldItem: ITEM, newItem: ITEM) -> Boolean) = { _, _ -> true }
    var dataStorage: DataStorage<ITEM> = SimpleDataProviderImpl()
        set(value) {
            field = value
            postRecyclerViewUpdate { notifyDataSetChanged() }
        }

    var isLoadingEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                postRecyclerViewUpdate { notifyItemChanged(itemCount - 1) }
            }
        }
    var onDataChangedListener = { itemsCount: Int -> }


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

    /////////////////////////////////////// SAVING STATE LOGIC /////////////////////////////////////

    fun saveState(outState: Bundle) {
        outState.putSerializable(stateId, dataStorage.toCollection(ArrayList()))
    }

    @Suppress("UNCHECKED_CAST")
    fun restoreState(inState: Bundle) {
        val data = inState.getSerializable(stateId) as List<ITEM>?
        data?.apply { dataStorage.set(this) }
    }

    private val stateId: String get() = EXTRA_STATE_PREFIX + this::class.java.name

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
    final override fun getItemViewType(position: Int): Int {

        val firstItem = if (reverseOrder) {
            if (isLoadingEnabled) TYPE_LOADING_ENABLED else TYPE_LOADING_DISABLED
        } else TYPE_HEADER
        val lastItem = if (reverseOrder) {
            TYPE_HEADER
        } else {
            if (isLoadingEnabled) TYPE_LOADING_ENABLED else TYPE_LOADING_DISABLED
        }

        return when (position) {
            0 -> firstItem
            itemCount - 1 -> lastItem
            else -> getViewType(position - emptyHeaderItemsCount)
        }
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

    abstract class BaseVH<in ITEM>(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        abstract fun bind(item: ITEM)
        override val containerView: View? get() = itemView
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
        fun isEmpty() = size == 0
    }

    ////////////////////////////////////////// DATA STORAGE ////////////////////////////////////////

    open inner class SimpleDataProviderImpl : ArrayList<ITEM>(), DataStorage<ITEM> {
        override fun clear() {
            postDataUpdate {
                val previousSize = size
                super.clear()
                if (previousSize != 0) notifyItemRangeRemoved(emptyHeaderItemsCount, previousSize)
            }
        }

        override fun add(element: ITEM): Boolean {
            postDataUpdate {
                super.add(element)
                notifyItemInserted(itemCount - emptyBottomItemsCount - 1)
            }
            return true
        }

        override fun addAll(elements: Collection<ITEM>): Boolean {
            postDataUpdate {
                val newDataList = ArrayList(this)
                newDataList.addAll(elements)

                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(newDataList)), true)
                super.clear()
                super.addAll(newDataList)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
            }
            return true
        }

        override fun set(elements: List<ITEM>) {
            postDataUpdate {
                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(elements)), true)
                super.clear()
                super.addAll(elements)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
            }
        }

        override fun remove(element: ITEM): Boolean {
            postDataUpdate {
                val newDataList = ArrayList(this)
                newDataList.remove(element)

                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(newDataList)), true)
                super.clear()
                super.addAll(newDataList)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
            }
            return true
        }

        override fun removeAll(elements: Collection<ITEM>): Boolean {
            postDataUpdate {
                val newDataList = ArrayList(this)
                newDataList.removeAll(elements)

                val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(this, ReadOnlyDataStorageWrapper(newDataList)), true)
                super.clear()
                super.addAll(newDataList)
                diffResult.dispatchUpdatesTo(this@BasePaginationRVAdapter)
            }
            return true
        }

        override fun isEmpty(): Boolean = size == 0
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

    ///////////////// FILTER /////////////
    open var searchPhrase = ""
    lateinit var searchListener: SearchListener<ITEM>
    open var filterPredicate: (item: ITEM) -> Boolean = { true }

    /////////////////   FILTER   ////////////////
    open class FilteredSortedDataStorage<ITEM>(private val filterPredicate: (item1: ITEM) -> Boolean,
                                               private val dataStorage: BasePaginationRVAdapter.DataStorage<ITEM>) : BasePaginationRVAdapter.DataStorage<ITEM> by dataStorage, SearchListener<ITEM> {

        override var containerList: List<ITEM> = emptyList()

        override fun search() {
            if (containerList.isEmpty() || containerList.size < dataStorage.size){
                containerList = dataStorage.toList()
            }
            val newValues = containerList.filter(filterPredicate)
            dataStorage.set(newValues)
        }
    }

    interface SearchListener<T> {
        var containerList: List<T>
        fun search()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    companion object {
        const val EXTRA_STATE_PREFIX = "EXTRA_STATE_PREFIX"
        const val MAX_STATE_SIZE = 75

        const val TYPE_UNDEFINED = -1
        const val TYPE_HEADER = 7778881
        const val TYPE_LOADING_ENABLED = 777888
        const val TYPE_LOADING_DISABLED = -888777
    }

    @PublishedApi
    internal var `access$recyclerView`: WeakStateExecutor<RecyclerView?, RecyclerView>
        get() = recyclerView
        set(value) {
            recyclerView = value
        }
}