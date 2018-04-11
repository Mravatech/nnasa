package com.mnassa.screen.chats.message.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.hhmm
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_message_my_with_reply.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class MyMessagesWithReplyViewHolder(itemView: View, private val onLongClickListener: View.OnLongClickListener, private val onReplyClick: (ChatMessageModel?, PostModel?) -> Unit) : BasePaginationRVAdapter.BaseVH<ChatMessageModel>(itemView) {
    override fun bind(item: ChatMessageModel) {
        itemView.tvMyMessageWithReply.text = item.text//todo add date to message
        itemView.tvMyMessageWithReply.setOnLongClickListener(onLongClickListener)
        itemView.tvMyMessageWithReply.tag = this@MyMessagesWithReplyViewHolder
        itemView.tvMyMessageWithReplySent.text = item.createdAt.hhmm()
        item.replyMessage?.second?.let {
            itemView.tvMyReplyMessage.text = it.text
        } ?: run {
            itemView.tvMyReplyMessage.text = item.replyPost?.second?.text
        }
        itemView.tvMyReplyMessage.setOnClickListener { onReplyClick(item.replyMessage?.second, item.replyPost?.second) }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onLongClickListener: View.OnLongClickListener, onReplyClick: (ChatMessageModel?, PostModel?) -> Unit): MyMessagesWithReplyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_my_with_reply, parent, false)
            return MyMessagesWithReplyViewHolder(view, onLongClickListener, onReplyClick)
        }
    }

}
