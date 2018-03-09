package com.mnassa.screen.connections.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connection_request.view.*
import kotlinx.android.synthetic.main.item_connection_request_more.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class NewConnectionRequestsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    private var moreItemsCount: Int = 0
    var onApplyClickListener = { account: ShortAccountModel -> }
    var onDeclineClickListener = { account: ShortAccountModel -> }
    var onShowAllClickListener = { }

    override fun set(list: List<ShortAccountModel>) {
        moreItemsCount = maxOf(list.size - MAX_ITEMS_COUNT, 0)
        if (list.size > MAX_ITEMS_COUNT) {
            super.set(list.subList(0, MAX_ITEMS_COUNT))
        } else {
            super.set(list)
        }
    }

    override fun createView(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
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
            R.id.btnApply -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onApplyClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.btnDecline -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onDeclineClickListener(getDataItemByAdapterPosition(position))
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
                tvPosition.text = item.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvPosition.visibility = if (tvPosition.text.isNullOrBlank()) View.GONE else View.VISIBLE

                btnApply.setOnClickListener(clickListener)
                btnApply.tag = this@UserViewHolder
                btnDecline.setOnClickListener(clickListener)
                btnDecline.tag = this@UserViewHolder
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

    private companion object {
        private const val MAX_ITEMS_COUNT = 3
        private const val TYPE_ITEM = 1
        private const val TYPE_MORE = 2
    }
}