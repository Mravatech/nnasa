package com.mnassa.screen.connections.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedFromEvent
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.invisibleIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connections_recommended.view.*
import kotlinx.android.synthetic.main.item_connections_recommended_more.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class RecommendedConnectionsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    private var moreItemsCount: Int = 0
    var onConnectClickListener = { account: ShortAccountModel -> }
    var onItemClickListener = { account: ShortAccountModel -> }
    var onShowAllClickListener = {}
    override var filterPredicate: (item: ShortAccountModel) -> Boolean = { it.formattedName.toLowerCase().contains(searchPhrase.toLowerCase()) }

    var onAfterSearchListener = { size: Int ->  }
    init {
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl())
        searchListener = dataStorage as SearchListener<ShortAccountModel>
        onDataChangedListener = { onAfterSearchListener(moreItemsCount) }
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        val newValues = searchListener.containerList.filter(filterPredicate)
        setWithMaxRange(newValues)
    }

    fun destoryCallbacks() {
        onConnectClickListener = { }
        onItemClickListener = { }
        onShowAllClickListener = { }
    }

    fun setWithMaxRange(list: List<ShortAccountModel>) {
        val maxItemsCount = MAX_RECOMMENDED_ITEMS_COUNT
        if (searchListener.containerList.isEmpty()) {
            searchListener.containerList = list
        }
        moreItemsCount = maxOf(list.size - maxItemsCount, 0)
        if (list.size > maxItemsCount) {
            super.set(list.subList(0, maxItemsCount))
        } else {
            super.set(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return when (viewType) {
            TYPE_ITEM -> UserViewHolder.newInstance(parent, this)
            TYPE_MORE -> MoreViewHolder.newInstance(parent, this)
            else -> throw IllegalArgumentException("Illegal view type $viewType")
        }
    }

    override fun getViewType(position: Int): Int {
        return if (moreItemsCount > 0 && position == (dataStorage.size - 1)) {
            TYPE_MORE
        } else TYPE_ITEM
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnConnect -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onConnectClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.btnShowMore -> {
                onShowAllClickListener()
            }
            R.id.cvRoot -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemClickListener(getDataItemByAdapterPosition(position))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BaseVH<ShortAccountModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is MoreViewHolder) {
            holder.setMoreCounter(moreItemsCount)
        }
    }

    private class UserViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarSquare(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedPosition
                tvPosition.invisibleIfEmpty()

                tvEventName.text = item.formattedFromEvent
                tvEventName.invisibleIfEmpty()

                btnConnect.setOnClickListener(onClickListener)
                btnConnect.tag = this@UserViewHolder
                btnConnect.text = fromDictionary(R.string.tab_connections_recommended_connect)

                cvRoot.setOnClickListener(onClickListener)
                cvRoot.tag = this@UserViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_recommended, parent, false)
                return UserViewHolder(view, onClickListener)
            }
        }
    }

    private class MoreViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                btnShowMore.setOnClickListener(onClickListener)
                btnShowMore.text = fromDictionary(R.string.tab_connections_recommended_show_all)
            }
        }

        fun setMoreCounter(count: Int) {
            itemView.tvMoreCount.text = "+${count + 1}"
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): MoreViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_recommended_more, parent, false)
                return MoreViewHolder(view, onClickListener)
            }
        }
    }

    private companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_MORE = 2
        private const val MAX_RECOMMENDED_ITEMS_COUNT = 11
    }
}