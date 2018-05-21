package com.mnassa.screen.group.list.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedName
import com.mnassa.extensions.formattedType
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_connections_all.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class AllGroupsRecyclerViewAdapter(private val withHeader: Boolean = false) : BasePaginationRVAdapter<GroupModel>(), View.OnClickListener {
    var onItemOptionsClickListener = { account: GroupModel, sender: View -> }
    var onItemClickListener = { account: GroupModel -> }
    var onBindHeader = { header: View -> }

    fun destroyCallbacks() {
        onItemOptionsClickListener = { _, _ -> }
        onBindHeader = { }
        onItemClickListener = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GroupModel> =
            GroupViewHolder.newInstance(parent, this)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<GroupModel> {
        return if (viewType == TYPE_HEADER && withHeader) HeaderHolder.newInstance(parent) else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<GroupModel>, position: Int) {
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
                val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_groups_header, parent, false)
                return HeaderHolder(view)
            }
        }
    }

    private class GroupViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GroupModel>(itemView) {

        override fun bind(item: GroupModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedType
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
            fun newInstance(parent: ViewGroup, clickListener: View.OnClickListener): GroupViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_all, parent, false)
                return GroupViewHolder(view, clickListener)
            }
        }
    }
}