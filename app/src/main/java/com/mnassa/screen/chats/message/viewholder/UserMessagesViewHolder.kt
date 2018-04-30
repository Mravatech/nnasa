package com.mnassa.screen.chats.message.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.extensions.formatAsTime
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_message_user.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class UserMessagesViewHolder(itemView: View, private val onLongClickListener: View.OnLongClickListener) : BasePaginationRVAdapter.BaseVH<ChatMessageModel>(itemView) {
    override fun bind(item: ChatMessageModel) {
        itemView.tvUserMessage.text = item.text
        itemView.tvUserMessage.setOnLongClickListener(onLongClickListener)
        itemView.tvUserMessage.tag = this@UserMessagesViewHolder
        itemView.tvMyMessageSent.text = item.createdAt.formatAsTime()
    }

    companion object {
        fun newInstance(parent: ViewGroup, onLongClickListener: View.OnLongClickListener): UserMessagesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_user, parent, false)
            return UserMessagesViewHolder(view, onLongClickListener)
        }
    }

}
