package com.mnassa.screen.chats.message.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.Post
import com.mnassa.extensions.hhmm
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_message_user_with_reply.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class UserMessagesWithReplyViewHolder(itemView: View, private val onLongClickListener: View.OnLongClickListener, private val onReplyClick: (ChatMessageModel?, Post?) -> Unit) : BasePaginationRVAdapter.BaseVH<ChatMessageModel>(itemView) {
    override fun bind(item: ChatMessageModel) {
        itemView.tvUserMessageWithReply.text = item.text
        itemView.tvUserMessageWithReply.setOnLongClickListener(onLongClickListener)
        itemView.tvUserMessageWithReply.tag = this@UserMessagesWithReplyViewHolder
        itemView.tvUserMessageWithReplySent.text = item.createdAt.hhmm()
        item.replyMessage?.second?.let {
            itemView.tvUserReplyMessage.text = it.text
        } ?: run {
            itemView.tvUserReplyMessage.text = item.replyPost?.second?.text
        }
        itemView.tvUserReplyMessage.setOnClickListener { onReplyClick(item.replyMessage?.second, item.replyPost?.second) }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onLongClickListener: View.OnLongClickListener, onReplyClick: (ChatMessageModel?, Post?) -> Unit): UserMessagesWithReplyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_user_with_reply, parent, false)
            return UserMessagesWithReplyViewHolder(view, onLongClickListener, onReplyClick)
        }
    }

}
