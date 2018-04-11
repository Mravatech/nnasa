package com.mnassa.screen.base.adapter.new

import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.core.addons.WeakStateExecutor
import java.lang.ref.WeakReference

/**
 * Created by Peter on 4/5/2018.
 */
class BaseAdapter private constructor(
        internal val dataSource: MutableDataSource,
        private val onClickListener: (view: View, position: Int) -> Unit,
        private val updateCallbackWrapper: ConnectableListUpdateCallbackWrapper) {

    companion object {
        fun builder() = AdapterBuilder()

        class AdapterBuilder internal constructor() {
            private lateinit var dataStorage: MutableDataSource
            private val updateCallbackWrapper = ConnectableListUpdateCallbackWrapper(WeakReference(null))
            private var clickListener: (view: View, position: Int) -> Unit = { _, _ -> }

            fun sorted() = this.also { dataStorage = SortedDataSource(updateCallbackWrapper) }
            fun simple() = this.also { dataStorage = SimpleDataSource(updateCallbackWrapper) }
            fun click(listener: (view: View, position: Int) -> Unit) = this.also { clickListener = listener }

            fun build(): BaseAdapter = BaseAdapter(dataStorage, clickListener, updateCallbackWrapper)
        }

        val BaseAdapter.asRVAdapter: RecyclerView.Adapter<*>
            get() {
                val adapter = BaseAdapterRVAdapter(this)
                this.updateCallbackWrapper.delegate = WeakReference(AdapterListUpdateCallback(adapter))
                return adapter
            }
    }

    private class BaseAdapterRVAdapter(private val baseAdapter: BaseAdapter) :
            RecyclerView.Adapter<BaseAdapterRVAdapter.ViewHolder>() {

        private val intToViewTypeMap = SparseArray<AdapterItemViewType<*>>()
        private lateinit var layoutInflater: LayoutInflater
        private val recyclerView = WeakStateExecutor<RecyclerView?, RecyclerView>(
                initState = null,
                executionPredicate = { it != null })
        private val wrappedDataSource by lazy {
            AsyncDataSourceWrapper(baseAdapter.dataSource, { action -> recyclerView.invoke { it.post(action) } })
        }
        private val dataStorage: MutableDataSource get() = wrappedDataSource

        override fun getItemCount(): Int = dataStorage.size

        override fun getItemViewType(position: Int): Int {
            val viewType = dataStorage[position].viewType
            intToViewTypeMap.put(viewType.id, viewType)
            return viewType.id
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = dataStorage[position].viewType.bind(holder.itemView, dataStorage[position])

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            if (!this::layoutInflater.isInitialized) {
                layoutInflater = LayoutInflater.from(parent.context)
            }

            lateinit var holder: ViewHolder
            val onClickListener = View.OnClickListener { view ->
                val position = holder.adapterPosition
                if (position >= 0) {
                    baseAdapter.onClickListener(view, position)
                }
            }

            val view = intToViewTypeMap.get(viewType).inflate(parent, layoutInflater, onClickListener)
            holder = ViewHolder(view)
            return holder
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView.value = WeakReference(recyclerView)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            this.recyclerView.value.clear()
            this.recyclerView.clear()
            super.onDetachedFromRecyclerView(recyclerView)
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}
