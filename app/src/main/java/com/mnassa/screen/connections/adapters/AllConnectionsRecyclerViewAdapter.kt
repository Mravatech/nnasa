package com.mnassa.screen.connections.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedFromEvent
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.base.adapter.FilteredSortedDataStorage
import com.mnassa.screen.base.adapter.SearchListener
import kotlinx.android.synthetic.main.item_connections_all.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class AllConnectionsRecyclerViewAdapter(private val withHeader: Boolean = false) : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    var onItemOptionsClickListener = { account: ShortAccountModel, sender: View -> }
    var onItemClickListener = { account: ShortAccountModel -> }
    var onBindHeader = { header: View -> }

    override var filterPredicate: (item: ShortAccountModel) -> Boolean = { it.formattedName.toLowerCase().contains(searchPhrase.toLowerCase()) }

    init {
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl(), this)
        searchListener = dataStorage as SearchListener
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        searchListener.search()
    }

    fun destroyCallbacks() {
        onItemOptionsClickListener = { _, _ -> }
        onBindHeader = { }
        onItemClickListener = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> =
            UserViewHolder.newInstance(parent, this)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<ShortAccountModel> {
        return if (viewType == TYPE_HEADER && withHeader) HeaderHolder.newInstance(parent) else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<ShortAccountModel>, position: Int) {
        if (holder is HeaderHolder) onBindHeader(holder.itemView)
        else super.onBindViewHolder(holder, position)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnMoreOptions -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemOptionsClickListener(getDataItemByAdapterPosition(position), v)
                }
            }
            R.id.rvConnectionRoot -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemClickListener(getDataItemByAdapterPosition(position))
                }
            }
        }
    }

    private class HeaderHolder(itemView: View) : BaseVH<Any>(itemView) {
        override fun bind(item: Any) = Unit

        companion object {
            fun newInstance(parent: ViewGroup): HeaderHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_connections_header, parent, false)
                return HeaderHolder(view)
            }
        }
    }

    private class UserViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedPosition
                tvPosition.goneIfEmpty()

                tvEventName.text = item.formattedFromEvent
                tvEventName.goneIfEmpty()

                btnMoreOptions.setOnClickListener(clickListener)
                btnMoreOptions.tag = this@UserViewHolder

                rvConnectionRoot.setOnClickListener(clickListener)
                rvConnectionRoot.tag = this@UserViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, clickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_all, parent, false)
                return UserViewHolder(view, clickListener)
            }
        }
    }
}