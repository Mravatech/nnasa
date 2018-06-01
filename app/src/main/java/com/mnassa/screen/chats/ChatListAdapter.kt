package com.mnassa.screen.chats

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.toTimeAgo
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_room.view.*


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatListAdapter : BaseSortedPaginationRVAdapter<ChatRoomModel>(), View.OnClickListener {

    var onItemClickListener = { item: ChatRoomModel -> }

    override val itemsComparator: (item1: ChatRoomModel, item2: ChatRoomModel) -> Int = { first, second ->
        requireNotNull(first.chatMessageModel).createdAt.compareTo(requireNotNull(second.chatMessageModel).createdAt) * -1
    }
    override val itemClass: Class<ChatRoomModel> = ChatRoomModel::class.java

    override var filterPredicate: (item: ChatRoomModel) -> Boolean = {
        val account = it.account
        if (account != null) {
            account.formattedName.toLowerCase().contains(searchPhrase.toLowerCase())
        } else false
    }

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second ->
            first.chatMessageModel?.text == second.chatMessageModel?.text &&
                    first.viewedAtDate == second.viewedAtDate &&
                    first.unreadCount == second.unreadCount
        }
        dataStorage = FilteredSortedDataStorage(filterPredicate, ChatDataStorage(this))
        searchListener = dataStorage as SearchListener<ChatRoomModel>
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.llCharRoom -> {
                onItemClickListener(getDataItemByAdapterPosition(position))
            }
        }
    }

    fun searchByName(text: String) {
        searchPhrase = text
        searchListener.search()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ChatRoomModel> =
            ChatRoomViewHolder.newInstance(parent, this)

    private class ChatRoomViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<ChatRoomModel>(itemView) {
        override fun bind(item: ChatRoomModel) {
            itemView.llCharRoom.tag = this@ChatRoomViewHolder
            itemView.llCharRoom.setOnClickListener(onClickListener)
            itemView.tvLastMessage.text = item.chatMessageModel?.text
            item.account?.let {
                itemView.ivChatUserIcon.avatarRound(it.avatar)
                itemView.tvUserName.text = it.formattedName
            }
            item.takeIf { it.unreadCount > 0 }?.let {
                itemView.tvMessageUnread.visibility = View.VISIBLE
                itemView.tvMessageUnread.text = it.unreadCount.toString()
                itemView.tvUserName.setTypeface(itemView.tvUserName.typeface, Typeface.BOLD)
                itemView.tvLastMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            } ?: kotlin.run {
                itemView.tvMessageUnread.visibility = View.GONE
                itemView.tvUserName.setTypeface(itemView.tvUserName.typeface, Typeface.NORMAL)
                itemView.tvLastMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_cool))
            }
            val ago = requireNotNull(item.chatMessageModel).createdAt.toTimeAgo()
            itemView.tvMessageCame.text = ago
        }


        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): ChatRoomViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
                return ChatRoomViewHolder(view, onClickListener)
            }
        }
    }

    class ChatDataStorage(private val adapter: BaseSortedPaginationRVAdapter<ChatRoomModel>) :
            SortedDataStorage<ChatRoomModel>(ChatRoomModel::class.java, adapter), DataStorage<ChatRoomModel> {
        private val idToModel = HashMap<String, ChatRoomModel>()

        override fun addAll(elements: Collection<ChatRoomModel>): Boolean {
            elements.forEach {
                add(it)
            }
            return true
        }

        override fun add(element: ChatRoomModel): Boolean {
            adapter.postUpdate {
                wrappedList.beginBatchedUpdates()
                val oldEntity = idToModel[element.id]
                if (oldEntity != null && wrappedList.indexOf(oldEntity) != -1) {
                    val position = wrappedList.indexOf(oldEntity)
                    wrappedList.updateItemAt(position, element)
                } else {
                    wrappedList.add(element)
                }
                idToModel[element.id] = element
                wrappedList.endBatchedUpdates()
            }
            return true
        }

    }

}