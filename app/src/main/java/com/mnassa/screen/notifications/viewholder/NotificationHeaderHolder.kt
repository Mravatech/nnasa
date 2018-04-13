package com.mnassa.screen.notifications.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.NotificationModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_notification_header.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationHeaderHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<NotificationModel>(itemView) {

    override fun bind(item: NotificationModel) {
        with(itemView) {
            val headerType = if (item.isOld) fromDictionary(R.string.notifications_old) else fromDictionary(R.string.notifications_new)
            tvNotificationHeader.text = headerType
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup): NotificationHeaderHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_header, parent, false)
            return NotificationHeaderHolder(view)
        }
    }
}

