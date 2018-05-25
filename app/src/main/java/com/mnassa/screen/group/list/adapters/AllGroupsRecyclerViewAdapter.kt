package com.mnassa.screen.group.list.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_connections_all.view.*
import kotlinx.android.synthetic.main.item_group_creator_header.view.*

typealias GI = AllGroupsRecyclerViewAdapter.GroupItem

/**
 * Created by Peter on 3/7/2018.
 */
class AllGroupsRecyclerViewAdapter(
        private val withHeader: Boolean = false,
        private val withGroupOptions: Boolean = true) : BasePaginationRVAdapter<GI>(), View.OnClickListener {
    var onItemOptionsClickListener = { account: GroupModel, sender: View -> }
    var onItemClickListener = { account: GroupModel -> }
    var onBindHeader = { header: View -> }

    fun destroyCallbacks() {
        onItemOptionsClickListener = { _, _ -> }
        onBindHeader = { }
        onItemClickListener = { }
    }

    fun setGroups(items: List<GroupModel>) {
        val result = ArrayList<GI>(items.size + 2)
        val createdByMeGroups = items.filter { it.isMyGroup() }
        if (createdByMeGroups.isNotEmpty()) {
            result += GroupItemHeader(true, createdByMeGroups.size)
            result += createdByMeGroups.map { GI(it, true) }
        }

        result += GroupItemHeader(false, items.size)
        result += items.map { GI(it, false) }
        set(result)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GI> {
        return when (viewType) {
            HEADER_ALL_GROUPS -> GroupCreatorHeaderHolder.newInstance(parent)
            HEADER_MY_GROUPS -> GroupCreatorHeaderHolder.newInstance(parent)
            else -> GroupViewHolder.newInstance(parent, this, withGroupOptions)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<GI> {
        return if (viewType == TYPE_HEADER && withHeader) HeaderHolder.newInstance(parent)
        else super.onCreateViewHolder(parent, viewType)
    }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        return when (item) {
            is GroupItemHeader -> if (item.isMyGroup) HEADER_MY_GROUPS else HEADER_ALL_GROUPS
            else -> super.getViewType(position)
        }
    }

    override fun onBindViewHolder(holder: BaseVH<GI>, position: Int) {
        if (holder is HeaderHolder) {
            onBindHeader(holder.itemView)
        } else super.onBindViewHolder(holder, position)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnMoreOptions -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    getDataItemByAdapterPosition(position).group?.let { onItemOptionsClickListener(it, v) }
                }
            }
            R.id.rvConnectionRoot -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    getDataItemByAdapterPosition(position).group?.let(onItemClickListener)
                }
            }
        }
    }

    open class GroupItem(
            val group: GroupModel?,
            val isMyGroup: Boolean)

    class GroupItemHeader(isMyGroup: Boolean, val count: Int)
        : GroupItem(null, isMyGroup)

    private class HeaderHolder(itemView: View) : BaseVH<Any>(itemView) {
        override fun bind(item: Any) = Unit

        companion object {
            fun newInstance(parent: ViewGroup): HeaderHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_groups_header, parent, false)
                return HeaderHolder(view)
            }
        }
    }

    private class GroupCreatorHeaderHolder(itemView: View) : BaseVH<GI>(itemView) {
        override fun bind(item: GI) {
            item as GroupItemHeader
            if (item.isMyGroup) {
                itemView.tvGroupCreatorHeader.setHeaderWithCounter(R.string.group_item_header_my, item.count)
            } else {
                itemView.tvGroupCreatorHeader.setHeaderWithCounter(R.string.group_item_header_all, item.count)
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup): GroupCreatorHeaderHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_creator_header, parent, false)
                return GroupCreatorHeaderHolder(view)
            }
        }
    }

    private class GroupViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GI>(itemView) {

        override fun bind(item: GI) {
            val item = requireNotNull(item.group)
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedRole
                tvPosition.goneIfEmpty()

                tvEventName.text = ""
                tvEventName.goneIfEmpty()

                btnMoreOptions.setOnClickListener(clickListener)
                btnMoreOptions.tag = this@GroupViewHolder

                rvConnectionRoot.setOnClickListener(clickListener)
                rvConnectionRoot.tag = this@GroupViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, clickListener: View.OnClickListener, withOptions: Boolean): GroupViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_all, parent, false)
                view.btnMoreOptions.isGone = !withOptions
                return GroupViewHolder(view, clickListener)
            }
        }
    }

    private companion object {
        const val HEADER_MY_GROUPS = 33
        const val HEADER_ALL_GROUPS = 34
    }
}