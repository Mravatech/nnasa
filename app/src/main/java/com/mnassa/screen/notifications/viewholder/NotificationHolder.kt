package com.mnassa.screen.notifications.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.NotificationModel
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.toTimeAgo
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_notifications.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<NotificationModel>(itemView) {

    override fun bind(item: NotificationModel) {
        with(itemView) {
            ivUserIcon.avatarRound(item.extra.avatar)
            tvUserName.text = item.extra.userName
            tvNotificationInfo.text = item.text
            tvNotificationCame.text = item.createdAt.toTimeAgo()
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NotificationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
            return NotificationHolder(view, onClickListener)
        }
    }

}