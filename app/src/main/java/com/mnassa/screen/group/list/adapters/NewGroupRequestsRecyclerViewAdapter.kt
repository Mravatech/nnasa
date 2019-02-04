package com.mnassa.screen.group.list.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connection_request.view.*
import kotlinx.android.synthetic.main.item_connection_request_more.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class NewGroupRequestsRecyclerViewAdapter : BasePaginationRVAdapter<GroupModel>(), View.OnClickListener {
    private var moreItemsCount: Int = 0
    var onAcceptClickListener = { group: GroupModel -> }
    var onDeclineClickListener = { group: GroupModel -> }
    var onItemClickListener = { group: GroupModel -> }
    var onShowAllClickListener = { }

    fun destroyCallbacks() {
        onAcceptClickListener = { }
        onDeclineClickListener = { }
        onShowAllClickListener = { }
        onItemClickListener = { }
    }

    fun setWithMaxRange(list: List<GroupModel>, maxItemsCount: Int) {
        val maxItemsCountWithCounter = maxItemsCount + 1

        moreItemsCount = maxOf(list.size - maxItemsCountWithCounter, 0)
        if (list.size > maxItemsCountWithCounter) {
            super.set(list.subList(0, maxItemsCountWithCounter))
        } else {
            super.set(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GroupModel> {
        return when (viewType) {
            TYPE_ITEM -> GroupViewHolder.newInstance(parent, this)
            TYPE_MORE -> MoreViewHolder.newInstance(parent, this)
            else -> throw IllegalArgumentException("Illegal view type $viewType")
        }
    }

    override fun getViewType(position: Int): Int {
        return if (moreItemsCount > 0 && position == (dataStorage.size - 1)) {
            TYPE_MORE
        } else TYPE_ITEM
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAccept -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onAcceptClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.btnDecline -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onDeclineClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.rlClickableRoot -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemClickListener(getDataItemByAdapterPosition(position))
                }
            }
            R.id.btnShowMore -> onShowAllClickListener()
        }
    }

    override fun onBindViewHolder(holder: BaseVH<GroupModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is MoreViewHolder) {
            holder.setMoreCounter(moreItemsCount)
        }
    }

    private class GroupViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GroupModel>(itemView) {

        override fun bind(item: GroupModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = ""
                tvPosition.goneIfEmpty()

                tvEventName.text = ""
                tvEventName.goneIfEmpty()

                btnAccept.setOnClickListener(clickListener)
                btnAccept.tag = this@GroupViewHolder
                btnDecline.setOnClickListener(clickListener)
                btnDecline.tag = this@GroupViewHolder
                rlClickableRoot.setOnClickListener(clickListener)
                rlClickableRoot.tag = this@GroupViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): GroupViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_request, parent, false)
                return GroupViewHolder(view, onClickListener)
            }
        }
    }

    private class MoreViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GroupModel>(itemView) {

        override fun bind(item: GroupModel) {
            itemView.btnShowMore.setOnClickListener(clickListener)
        }

        fun setMoreCounter(count: Int) {
            itemView.btnShowMore.text = fromDictionary(R.string.group_invites_more).format(count + 1)
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): MoreViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_request_more, parent, false)
                return MoreViewHolder(view, onClickListener)
            }
        }
    }


    private companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_MORE = 2
    }
}