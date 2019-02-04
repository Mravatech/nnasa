package com.mnassa.screen.chats.message

import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.RecyclerView
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
import com.mnassa.screen.chats.message.MessagesAdapter.Companion.DATE_CREATOR
import com.mnassa.screen.chats.message.viewholder.*
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class MessagesAdapter : BaseSortedPaginationRVAdapter<ChatMessageModel>(), View.OnClickListener, View.OnLongClickListener {

    lateinit var accountId: String
    var onMyMessageLongClick = { item: ChatMessageModel -> }
    var onUserMessageLongClick = { item: ChatMessageModel -> }
    var onReplyClick = { chatModel: ChatMessageModel?, post: PostModel? -> }

    override val itemsComparator: (item1: ChatMessageModel, item2: ChatMessageModel) -> Int = { first, second ->
        val res = when {
            first.createdAt == second.createdAt -> 0
            first.creator == DATE_CREATOR && second.creator == DATE_CREATOR -> {
                if (first.createdAt == second.createdAt) 0 else first.createdAt.time.compareTo(second.createdAt.time)
            }
            first.creator == DATE_CREATOR && second.creator != DATE_CREATOR -> {
                if (first.createdAt.isTheSameDay(second.createdAt)) -1 else first.createdAt.time.compareTo(second.createdAt.time)
            }
            else -> first.createdAt.time.compareTo(second.createdAt.time)
        }
        res * -1
    }
    override val itemClass: Class<ChatMessageModel> = ChatMessageModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = ChatDataStorage(this)
    }

    fun destroyCallbacks() {
        onMyMessageLongClick = {}
        onUserMessageLongClick = {}
        onReplyClick = { _, _ -> }
        onDataChangedListener = {}
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
                false -> MY_MESSAGE
                true -> MY_MESSAGE_WITH_REPLY
            }
            else -> when (item.replyMessage?.second != null || item.replyPost?.second != null) {
                false -> USER_MESSAGE
                true -> USER_MESSAGE_WITH_REPLY
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

    class ChatDataStorage(adapter: MessagesAdapter) :
            SortedDataStorage<ChatMessageModel>(ChatMessageModel::class.java, adapter), DataStorage<ChatMessageModel> {
        private val dateMessages = HashMap<Date, ChatMessageModel>()

        override fun addAll(elements: Collection<ChatMessageModel>): Boolean {
            for (element in elements) {
                val dateElement = dateMessages[element.createdAt.getStartOfDay()]
                if (dateElement == null) {
                    val dateMessage = createDateMessage(element)
                    dateMessages[dateMessage.createdAt.getStartOfDay()] = dateMessage
                } else if (!dateElement.createdAt.isTheSameDay(element.createdAt) && dateElement.createdAt > element.createdAt) {
                    super.remove(dateElement)
                    val dateMessage = createDateMessage(element)
                    dateMessages[dateMessage.createdAt.getStartOfDay()] = dateMessage
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
            } else if (!dateElement.createdAt.isTheSameDay(element.createdAt) && dateElement.createdAt < element.createdAt) {
                super.remove(dateElement)
                addDateMessage(element)
            }
            return super.add(element)
        }

        override fun remove(element: ChatMessageModel): Boolean {
            val position = wrappedList.indexOf(element)
            if (position < 0) return false

            // # positions
            // (size-1) -- always header       | must never happen; just remove element
            // 2        -- header or message   | if prev. AND nex message is header - remove prev. header & element
            // 1        -- header or message   | if prev. AND nex message is header - remove prev. header & element
            // 0        -- always message      | if prev. message is header         - remove prev. header & element

            val previousElement = wrappedList.getOrNull(position + 1)
            val nextElement = wrappedList.getOrNull(position - 1)

            if (previousElement != null && (nextElement == null && previousElement.isHeader || previousElement.isHeader && nextElement.isHeader)) {
                dateMessages.remove(previousElement.createdAt.getStartOfDay())
                super.remove(previousElement)
            }

            return super.remove(element)
        }

        private fun addDateMessage(element: ChatMessageModel) {
            val dateMessage = createDateMessage(element)
            super.add(dateMessage)
            dateMessages[dateMessage.createdAt.getStartOfDay()] = dateMessage
        }

        private fun createDateMessage(element: ChatMessageModel): ChatMessageModel =
                ChatMessageModelImpl(
                        createdAt = Date(element.createdAt.time - 1),
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

fun <T> SortedList<T>.getOrNull(position: Int): T? {
    if (position >= 0 && position < size()) return get(position)
    return null
}

inline val ChatMessageModel?.isHeader: Boolean get() = this != null && creator == DATE_CREATOR

