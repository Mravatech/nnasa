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
import java.io.Serializable

typealias GroupItem = AllGroupsRecyclerViewAdapter.GroupItem

/**
 * Created by Peter on 3/7/2018.
 */
class AllGroupsRecyclerViewAdapter(
        var withHeader: Boolean = false,
        private val withGroupOptions: Boolean = true) : BasePaginationRVAdapter<GroupItem>(), View.OnClickListener {
    var onItemOptionsClickListener = { account: GroupModel, sender: View -> }
    var onItemClickListener = { account: GroupModel -> }
    var onBindHeader = { header: View -> }
    var onSearchClickListener = { }
    private var originalData: List<GroupItem> = emptyList()

    fun destroyCallbacks() {
        onItemOptionsClickListener = { _, _ -> }
        onBindHeader = { }
        onItemClickListener = { }
        onSearchClickListener = { }
    }

    fun setGroups(items: List<GroupModel>) {
        val result = ArrayList<GroupItem>(items.size + 2)
        val createdByMeGroups = items.filter { it.isMyGroup() }
        if (createdByMeGroups.isNotEmpty()) {
            result += GroupItemHeader(true, createdByMeGroups.size)
            result += createdByMeGroups.map { GroupItem(it, true) }
        }

        result += GroupItemHeader(false, items.size)
        result += items.map { GroupItem(it, false) }
        originalData = result
        set(result)
    }

    fun searchByName(text: String) {
        val query = text.toLowerCase()
        val filteredData = originalData.filter {
            it !is GroupItemHeader && !it.isMyGroup && (query.isBlank() || it.group?.name?.toLowerCase()?.contains(query) == true)
        }
        set(filteredData)
    }

    fun finishSearch() = set(originalData)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GroupItem> {
        return when (viewType) {
            HEADER_ALL_GROUPS -> GroupCreatorHeaderHolder.newInstance(parent, this)
            HEADER_MY_GROUPS -> GroupCreatorHeaderHolder.newInstance(parent, this)
            else -> GroupViewHolder.newInstance(parent, this, withGroupOptions)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<GroupItem> {
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

    override fun onBindViewHolder(holder: BaseVH<GroupItem>, position: Int) {
        if (holder is HeaderHolder) {
            onBindHeader(holder.itemView)
        } else super.onBindViewHolder(holder, position)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnMoreOptions -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    getDataItemByAdapterPosition(position).group?.let { onItemOptionsClickListener(it, view) }
                }
            }
            R.id.rvConnectionRoot -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    getDataItemByAdapterPosition(position).group?.let(onItemClickListener)
                }
            }
            R.id.ivSearch -> {
                onSearchClickListener()
            }
        }
    }

    open class GroupItem(
            val group: GroupModel?,
            val isMyGroup: Boolean) : Serializable

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

    private class GroupCreatorHeaderHolder(itemView: View) : BaseVH<GroupItem>(itemView) {
        override fun bind(item: GroupItem) {
            item as GroupItemHeader
            if (item.isMyGroup) {
                itemView.tvGroupCreatorHeader.setHeaderWithCounter(R.string.group_item_header_my, item.count)
                itemView.ivSearch.isGone = true
            } else {
                itemView.tvGroupCreatorHeader.setHeaderWithCounter(R.string.group_item_header_all, item.count)
                itemView.ivSearch.isGone = false
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): GroupCreatorHeaderHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_creator_header, parent, false)
                val holder = GroupCreatorHeaderHolder(view)
                view.ivSearch.tag = holder
                view.ivSearch.setOnClickListener(onClickListener)
                return holder
            }
        }
    }

    private class GroupViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GroupItem>(itemView) {

        override fun bind(item: GroupItem) {
            val groupItem = requireNotNull(item.group)
            with(itemView) {
                ivAvatar.avatarRound(groupItem.avatar)
                tvUserName.text = groupItem.formattedName

                tvPosition.text = groupItem.formattedRole
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