package com.mnassa.screen.connections.archived

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connection_archived_header.view.*
import kotlinx.android.synthetic.main.item_connection_archived_item.view.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 9.03.2018.
 */
class ArchivedConnectionsRVAdapter : BasePaginationRVAdapter<DeclinedShortAccountModel>(), View.OnClickListener {

    var onConnectClickListener = { account: DeclinedShortAccountModel -> }

    var disconnectTimeoutDays: Int = 30
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnConnect -> {
                val vh = view.tag as RecyclerView.ViewHolder
                val position = vh.adapterPosition
                if (position >= 0) {
                    onConnectClickListener(getDataItemByAdapterPosition(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<DeclinedShortAccountModel> {
        return ArchivedConnectionItemViewHolder.newInstance(parent, this) { disconnectTimeoutDays }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<DeclinedShortAccountModel> {
        return if (viewType == TYPE_HEADER) ArchivedConnectionHeaderViewHolder.newInstance(parent) { disconnectTimeoutDays }
        else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<DeclinedShortAccountModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is ArchivedConnectionHeaderViewHolder) {
            holder.bind()
        }
    }

    private class ArchivedConnectionItemViewHolder(itemView: View, private val onClickListener: View.OnClickListener, private val disconnectTimeoutDays: () -> Int) :
            BaseVH<DeclinedShortAccountModel>(itemView) {

        override fun bind(item: DeclinedShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvPosition.goneIfEmpty()

                val daysLeft = getDaysLeftCount(item.declinedAt)
                tvDaysLeft.text = fromDictionary(R.string.archived_connections_days_left).format(daysLeft)
                tvDaysLeft.visibility = if (daysLeft > 0) View.VISIBLE else View.GONE
                btnConnect.visibility = if (daysLeft > 0) View.GONE else View.VISIBLE

                btnConnect.text = fromDictionary(R.string.archived_connections_connect_btn)
                btnConnect.setOnClickListener(onClickListener)
                btnConnect.tag = this@ArchivedConnectionItemViewHolder
            }
        }

        private fun getDaysLeftCount(declinedAt: Date): Int {
            val diffMillis = System.currentTimeMillis() - declinedAt.time
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
            return maxOf(0, disconnectTimeoutDays() - diffDays)
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, disconnectTimeoutDays: () -> Int): ArchivedConnectionItemViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_archived_item, parent, false)
                return ArchivedConnectionItemViewHolder(view, onClickListener, disconnectTimeoutDays)
            }
        }
    }

    private class ArchivedConnectionHeaderViewHolder(itemView: View, private val disconnectTimeoutDays: () -> Int) :
            BaseVH<DeclinedShortAccountModel>(itemView) {

        override fun bind(item: DeclinedShortAccountModel) = bind()

        fun bind() {
            with(itemView) {
                tvDescription.text = fromDictionary(R.string.archived_connections_description).format(disconnectTimeoutDays())
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, disconnectTimeoutDays: () -> Int): ArchivedConnectionHeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_archived_header, parent, false)
                return ArchivedConnectionHeaderViewHolder(view, disconnectTimeoutDays)
            }
        }
    }
}