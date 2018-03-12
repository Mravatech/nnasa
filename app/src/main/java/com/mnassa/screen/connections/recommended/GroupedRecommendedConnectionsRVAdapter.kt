package com.mnassa.screen.connections.recommended

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connection_request.view.*
import kotlinx.android.synthetic.main.item_connections_recommended_group.view.*

/**
 * Created by Peter on 11.03.2018.
 */
class GroupedRecommendedConnectionsRVAdapter : BasePaginationRVAdapter<GroupedConnection>(), View.OnClickListener {
    var onConnectClickListener = { account: ShortAccountModel -> }
    var onDeclineClickListener = { account: ShortAccountModel -> }

    fun set(input: RecommendedConnections) {
        val result = ArrayList<GroupedConnection>()
        if (input.byPhone.isNotEmpty()) {
            result += GroupedConnection.Group(fromDictionary(R.string.recommended_connections_by_phone))
            input.byPhone.values.forEach {
                it.forEach {
                    result += GroupedConnection.Connection(it)
                }
            }
        }

        if (input.byEvents.isNotEmpty()) {
            result += GroupedConnection.Group(fromDictionary(R.string.recommended_connections_by_event))
            input.byEvents.values.forEach {
                it.forEach {
                    result += GroupedConnection.Connection(it)
                }
            }
        }

        if (input.byGroups.isNotEmpty()) {
            result += GroupedConnection.Group(fromDictionary(R.string.recommended_connections_by_refer))
            input.byGroups.values.forEach {
                it.forEach {
                    result += GroupedConnection.Connection(it)
                }
            }
        }

        set(result)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnAccept -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    val item = getDataItemByAdapterPosition(position) as GroupedConnection.Connection
                    onConnectClickListener(item.account)
                }
            }
            R.id.btnDecline -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    val item = getDataItemByAdapterPosition(position) as GroupedConnection.Connection
                    onDeclineClickListener(item.account)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GroupedConnection> =
            when (viewType) {
                TYPE_ITEM -> UserViewHolder.newInstance(parent, this)
                TYPE_GROUP -> GroupViewHolder.newInstance(parent)
                else -> throw IllegalArgumentException("Illegal view type $viewType")
            }

    override fun getViewType(position: Int): Int = when (dataStorage.get(position)) {
        is GroupedConnection.Group -> TYPE_GROUP
        is GroupedConnection.Connection -> TYPE_ITEM
    }

    private class GroupViewHolder(itemView: View) : BaseVH<GroupedConnection>(itemView) {
        override fun bind(item: GroupedConnection) {
            itemView.tvGroupName.text = (item as GroupedConnection.Group).name
        }

        companion object {
            fun newInstance(parent: ViewGroup): GroupViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_recommended_group, parent, false)
                return GroupViewHolder(view)
            }
        }
    }

    private class UserViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GroupedConnection>(itemView) {
        override fun bind(item: GroupedConnection) {

            val data = (item as GroupedConnection.Connection).account

            with(itemView) {
                ivAvatar.avatarRound(data.avatar)
                tvUserName.text = data.formattedName
                tvPosition.text = data.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvPosition.goneIfEmpty()

                btnAccept.setOnClickListener(clickListener)
                btnAccept.tag = this@UserViewHolder
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

    private companion object {
        const val TYPE_GROUP = 1
        const val TYPE_ITEM = 2
    }
}

sealed class GroupedConnection {
    data class Group(val name: String) : GroupedConnection()
    data class Connection(val account: ShortAccountModel) : GroupedConnection()
}