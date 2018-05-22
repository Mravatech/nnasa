package com.mnassa.screen.group.list.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedName
import com.mnassa.extensions.formattedRole
import com.mnassa.extensions.invisibleIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connections_recommended.view.*
import kotlinx.android.synthetic.main.item_connections_recommended_more.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class RecommendedGroupsRecyclerViewAdapter : BasePaginationRVAdapter<GroupModel>(), View.OnClickListener {
    private var moreItemsCount: Int = 0
    var onConnectClickListener = { group: GroupModel -> }
    var onItemClickListener = { group: GroupModel -> }
    var onShowAllClickListener = {}

    fun destoryCallbacks() {
        onConnectClickListener = { }
        onItemClickListener = { }
        onShowAllClickListener = { }
    }

    fun setWithMaxRange(list: List<GroupModel>, maxItemsCount: Int) {
        val maxItemsCount = maxItemsCount + 1

        moreItemsCount = maxOf(list.size - maxItemsCount, 0)
        if (list.size > maxItemsCount) {
            super.set(list.subList(0, maxItemsCount))
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

    override fun onBindViewHolder(holder: BaseVH<GroupModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is MoreViewHolder) {
            holder.setMoreCounter(moreItemsCount)
        }
    }

    private class GroupViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<GroupModel>(itemView) {

        override fun bind(item: GroupModel) {
            with(itemView) {
                ivAvatar.avatarSquare(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedRole
                tvPosition.invisibleIfEmpty()

                tvEventName.text = ""
                tvEventName.invisibleIfEmpty()

                btnConnect.setOnClickListener(onClickListener)
                btnConnect.tag = this@GroupViewHolder
                btnConnect.text = fromDictionary(R.string.tab_connections_recommended_connect)

                cvRoot.setOnClickListener(onClickListener)
                cvRoot.tag = this@GroupViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): GroupViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_recommended, parent, false)
                return GroupViewHolder(view, onClickListener)
            }
        }
    }

    private class MoreViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<GroupModel>(itemView) {

        override fun bind(item: GroupModel) {
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
    }
}