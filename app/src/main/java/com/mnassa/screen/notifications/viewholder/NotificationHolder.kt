package com.mnassa.screen.notifications.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.AccountType
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

        if (item.extra?.author != null) {
            val name = if (item.extra?.author?.accountType == AccountType.PERSONAL) {
                "${item.extra?.author?.personalInfo?.firstName} ${item.extra?.author?.personalInfo?.lastName}"
            } else {
                item.extra?.author?.organizationInfo?.organizationName
            }
            itemView.tvUserName.text = name
        }

        with(itemView) {
            ivUserIcon.avatarRound(item.extra?.author?.avatar)
            tvNotificationInfo.text = item.text
            tvNotificationCame.text = item.createdAt.toTimeAgo()
            llNotificationRoot.setOnClickListener(onClickListener)
            llNotificationRoot.tag = this@NotificationHolder
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NotificationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
            return NotificationHolder(view, onClickListener)
        }
    }

}