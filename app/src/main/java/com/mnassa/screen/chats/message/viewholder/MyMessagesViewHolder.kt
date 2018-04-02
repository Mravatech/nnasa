package com.mnassa.screen.chats.message.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_message_my.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class MyMessagesViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<ChatMessageModel>(itemView) {
    override fun bind(item: ChatMessageModel) {

        itemView.tvMessage.text = item.text

    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): MyMessagesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message_my, parent, false)
            return MyMessagesViewHolder(view, onClickListener)
        }
    }

}
