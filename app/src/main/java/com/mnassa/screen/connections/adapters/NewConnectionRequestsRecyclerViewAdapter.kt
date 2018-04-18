package com.mnassa.screen.connections.adapters

import android.os.Bundle
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
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connection_request.view.*
import kotlinx.android.synthetic.main.item_connection_request_more.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class NewConnectionRequestsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    private var moreItemsCount: Int = 0
    var onAcceptClickListener = { account: ShortAccountModel -> }
    var onDeclineClickListener = { account: ShortAccountModel -> }
    var onItemClickListener = { account: ShortAccountModel -> }
    var onShowAllClickListener = { }

    fun destroyCallbacks() {
        onAcceptClickListener = { }
        onDeclineClickListener = { }
        onShowAllClickListener = { }
        onItemClickListener = { }
    }

    fun setWithMaxRange(list: List<ShortAccountModel>, maxItemsCount: Int) {
        val maxItemsCount = maxItemsCount + 1

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
            R.id.btnAccept -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onAcceptClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.btnDecline -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onDeclineClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.rlClickableRoot -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.btnShowMore -> onShowAllClickListener()
        }
    }

    override fun onBindViewHolder(holder: BaseVH<ShortAccountModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is MoreViewHolder) {
            holder.setMoreCounter(moreItemsCount)
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

                btnAccept.setOnClickListener(clickListener)
                btnAccept.tag = this@UserViewHolder
                btnDecline.setOnClickListener(clickListener)
                btnDecline.tag = this@UserViewHolder
                rlClickableRoot.setOnClickListener(clickListener)
                rlClickableRoot.tag = this@UserViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_request, parent, false)
                return UserViewHolder(view, onClickListener)
            }
        }
    }

    private class MoreViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) {
            itemView.btnShowMore.setOnClickListener(clickListener)
        }

        fun setMoreCounter(count: Int) {
            itemView.btnShowMore.text = fromDictionary(R.string.tab_connections_new_requests_more).format(count + 1)
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): MoreViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_request_more, parent, false)
                return MoreViewHolder(view, onClickListener)
            }
        }
    }

    fun saveState(outState: Bundle) {
        outState.putSerializable(EXTRA_STATE_NEW_CONNECTIONS, dataStorage.toCollection(ArrayList()))
    }

    @Suppress("UNCHECKED_CAST")
    fun restoreState(inState: Bundle) {
        dataStorage.set(inState.getSerializable(EXTRA_STATE_NEW_CONNECTIONS) as List<ShortAccountModel>)
    }

    private companion object {
        private const val EXTRA_STATE_NEW_CONNECTIONS = "EXTRA_STATE_NEW_CONNECTIONS"

        private const val TYPE_ITEM = 1
        private const val TYPE_MORE = 2
    }
}