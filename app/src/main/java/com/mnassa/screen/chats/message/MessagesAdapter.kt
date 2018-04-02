package com.mnassa.screen.chats.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.chats.message.viewholder.MyMessagesViewHolder
import com.mnassa.screen.chats.message.viewholder.UserMessagesViewHolder

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
class MessagesAdapter(private val accountId: String) : BaseSortedPaginationRVAdapter<ChatMessageModel>(), View.OnClickListener {
    override val itemsComparator: (item1: ChatMessageModel, item2: ChatMessageModel) -> Int = { first, second ->
        first.createdAt.compareTo(second.createdAt) * -1
    }
    override val itemClass: Class<ChatMessageModel> = ChatMessageModel::class.java

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ChatMessageModel> =
            when (viewType) {
                MY_MESSAGE -> MyMessagesViewHolder.newInstance(parent, this)
                USER_MESSAGE -> UserMessagesViewHolder.newInstance(parent, this)
                else -> throw IllegalStateException("Illegal view type $viewType")
            }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        return when (item.creator) {
            accountId -> USER_MESSAGE
            else -> MY_MESSAGE
        }
    }

    override fun onClick(v: View?) {

    }

    companion object {
        const val USER_MESSAGE = 0
        const val MY_MESSAGE = 1
    }

}