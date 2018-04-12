package com.mnassa.screen.chats.message

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.impl.ChatMessageModelImpl
import com.mnassa.extensions.getStartOfDay
import com.mnassa.extensions.isTheSameDay
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.chats.message.viewholder.*
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class MessagesAdapter(private val accountId: String) : BaseSortedPaginationRVAdapter<ChatMessageModel>(), View.OnClickListener, View.OnLongClickListener {

    var onMyMessageLongClick = { item: ChatMessageModel -> }
    var onUserMessageLongClick = { item: ChatMessageModel -> }
    var onReplyClick = { chatModel: ChatMessageModel?, post: PostModel? -> }

    override val itemsComparator: (item1: ChatMessageModel, item2: ChatMessageModel) -> Int = { first, second ->
        first.createdAt.compareTo(second.createdAt) * 1
    }
    override val itemClass: Class<ChatMessageModel> = ChatMessageModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = ChatDataStorage(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ChatMessageModel> {
        return when (viewType) {
            MY_MESSAGE -> MyMessagesViewHolder.newInstance(parent, this)
            USER_MESSAGE -> UserMessagesViewHolder.newInstance(parent, this)
            USER_MESSAGE_WITH_REPLY -> UserMessagesWithReplyViewHolder.newInstance(parent, this, onReplyClick)
            MY_MESSAGE_WITH_REPLY -> MyMessagesWithReplyViewHolder.newInstance(parent, this, onReplyClick)
            DATE_MESSAGE -> DateMessagesHolder.newInstance(parent)
            else -> throw IllegalStateException("Illegal view type $viewType")
        }
    }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        return when (item.creator) {
            DATE_CREATOR -> DATE_MESSAGE
            accountId -> when (item.replyMessage?.second != null || item.replyPost?.second != null) {
                false -> USER_MESSAGE
                true -> USER_MESSAGE_WITH_REPLY
            }
            else -> when (item.replyMessage?.second != null || item.replyPost?.second != null) {
                false -> MY_MESSAGE
                true -> MY_MESSAGE_WITH_REPLY
            }
        }
    }

    override fun onClick(v: View?) {

    }

    override fun onLongClick(view: View): Boolean {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return false
        when (view.id) {
            R.id.tvUserMessage, R.id.tvUserMessageWithReply -> {
                onUserMessageLongClick(getDataItemByAdapterPosition(position))
            }
            R.id.tvMyMessage, R.id.tvMyMessageWithReply -> {
                onMyMessageLongClick(getDataItemByAdapterPosition(position))
            }
        }
        return true
    }

    companion object {
        const val USER_MESSAGE = 0
        const val MY_MESSAGE = 1
        const val DATE_MESSAGE = 2
        const val MY_MESSAGE_WITH_REPLY = 3
        const val USER_MESSAGE_WITH_REPLY = 4
        const val DATE_CREATOR = "date"
        const val TEXT_TYPE = "text"
    }

    class ChatDataStorage(adapter: BaseSortedPaginationRVAdapter<ChatMessageModel>) :
            SortedDataStorage<ChatMessageModel>(ChatMessageModel::class.java, adapter), DataStorage<ChatMessageModel> {
        private val dateMessages = HashMap<Date, ChatMessageModel>()

        override fun addAll(elements: Collection<ChatMessageModel>): Boolean {
            for (element in elements){
                val dateElement = dateMessages[element.createdAt.getStartOfDay()]
                if (dateElement == null) {
                    val dateMessage = createDateMessage(element, element.createdAt.time - 1L)//for sorting to display above
                    dateMessages[dateMessage.createdAt.getStartOfDay()] = dateMessage
                    Timber.i("testtest1 ${element}")
                } else if (!dateElement.createdAt.isTheSameDay(element.createdAt) && dateElement.createdAt < element.createdAt) {
                    super.remove(dateElement)
                    val dateMessage = createDateMessage(element, element.createdAt.time - 1L)//for sorting to display above
                    dateMessages[dateMessage.createdAt.getStartOfDay()] = dateMessage
                    Timber.i("testtest2 ${element}")
                }
            }
            val dates: List<ChatMessageModel> = dateMessages.map { it.value }
            super.addAll(dates)
            return super.addAll(elements)
        }



        override fun removeAll(elements: Collection<ChatMessageModel>): Boolean {
            elements.forEach {
                remove(it)
            }
            return true
        }

        override fun add(element: ChatMessageModel): Boolean {
            val dateElement = dateMessages[element.createdAt.getStartOfDay()]
            if (dateElement == null) {
                addDateMessage(element)
                Timber.i("testtest1 ${element}")
            } else if (!dateElement.createdAt.isTheSameDay(element.createdAt) && dateElement.createdAt < element.createdAt) {
                super.remove(dateElement)
                addDateMessage(element)
                Timber.i("testtest2 ${element}")
            }
            return super.add(element)
        }

        override fun remove(element: ChatMessageModel): Boolean {
            val position = wrappedList.indexOf(element)
            val size = wrappedList.size()
            if (position < size && position > 0) {
                val prev = wrappedList.get(position - 1)
                if (prev.creator == DATE_CREATOR) {
                    super.remove(prev)
                }
            } else {
                val next = wrappedList.get(position + 1)
                val prev = wrappedList.get(position - 1)
                if (next.creator != DATE_CREATOR && prev.creator == DATE_CREATOR) {
                    super.remove(prev)
                }
            }
            return super.remove(element)
        }

        private fun addDateMessage(element: ChatMessageModel) {
            val dateMessage = createDateMessage(element, element.createdAt.time - 1L)//for sorting to display above
            super.add(dateMessage)
            dateMessages[dateMessage.createdAt.getStartOfDay()] = dateMessage
        }

        private fun createDateMessage(element: ChatMessageModel, time: Long): ChatMessageModel =
                ChatMessageModelImpl(
                        createdAt = Date(time),
                        creator = DATE_CREATOR,
                        text = "",
                        type = TEXT_TYPE,
                        chatID = null,
                        replyMessage = element.replyMessage,
                        replyPost = element.replyPost,
                        id = "${element.id}${element.createdAt.time}"
                )
    }
}

