package com.mnassa.screen.notifications.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.notifications.NotificationAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_notification_header.view.*

class NotificationHeaderHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<NotificationAdapter.NotificationItem>(itemView) {

    override fun bind(item: NotificationAdapter.NotificationItem) {
        val isNew = (item as NotificationAdapter.NotificationItem.HeaderItem).isNew

        with(itemView) {
            tvNotificationHeader.text = if (isNew)
                fromDictionary(R.string.notifications_new)
            else fromDictionary(R.string.notifications_old)
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup): NotificationHeaderHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_header, parent, false)
            return NotificationHeaderHolder(view)
        }
    }
}

