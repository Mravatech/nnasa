package com.mnassa.screen.chats.message.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.extensions.hhmm
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_message_my.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class MyMessagesViewHolder(itemView: View, private val onLongClickListener: View.OnLongClickListener) : BasePaginationRVAdapter.BaseVH<ChatMessageModel>(itemView) {
    override fun bind(item: ChatMessageModel) {
        itemView.tvMyMessage.text = item.text//todo add date to message
        itemView.tvMyMessage.setOnLongClickListener(onLongClickListener)
        itemView.tvMyMessage.tag = this@MyMessagesViewHolder
        itemView.tvMyMessageSent.text = item.createdAt.hhmm()
    }

    companion object {
        fun newInstance(parent: ViewGroup, onLongClickListener: View.OnLongClickListener): MyMessagesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_my, parent, false)
            return MyMessagesViewHolder(view, onLongClickListener)
        }
    }

}
