package com.mnassa.screen.chats

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.extensions.avatarRound
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import kotlinx.android.synthetic.main.item_chat_room.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatListAdapter : BaseSortedPaginationRVAdapter<ChatRoomModel>(), View.OnClickListener {

    var onItemClickListener = { item: ChatRoomModel -> }

    override val itemsComparator: (item1: ChatRoomModel, item2: ChatRoomModel) -> Int = { first, second ->
        first.viewedAtDate.compareTo(second.viewedAtDate) * -1
    }
    override val itemClass: Class<ChatRoomModel> = ChatRoomModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.lastMessageModel?.text == second.lastMessageModel?.text }
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = SortedDataStorage(itemClass, this)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ChatRoomModel> =
            ChatRoomViewHolder.newInstance(parent, this)

    private class ChatRoomViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<ChatRoomModel>(itemView) {
        override fun bind(item: ChatRoomModel) {
            itemView.llCharRoom.tag = this@ChatRoomViewHolder
            itemView.llCharRoom.setOnClickListener(onClickListener)
            itemView.ivChatUserIcon.avatarRound(item.lastMessageModel?.account?.avatar)
            itemView.tvLastMessage.text = item.lastMessageModel?.text
            itemView.tvUserName.text = item.lastMessageModel?.account?.userName
            itemView.tvMessageCame.text = item.viewedAt
            item.takeIf { it.unreadCount > 0 }?.let {
                itemView.tvMessageUnread.visibility = View.VISIBLE
                itemView.tvMessageUnread.text = it.unreadCount.toString()
                itemView.tvUserName.setTypeface(itemView.tvUserName.typeface, Typeface.BOLD)
                itemView.tvLastMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            } ?: kotlin.run {
                itemView.tvMessageUnread.visibility = View.GONE
                itemView.tvUserName.setTypeface(itemView.tvUserName.typeface, Typeface.NORMAL)
                itemView.tvLastMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.coolGray))
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())//todo move from here
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val time = item.viewedAtDate
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            itemView.tvMessageCame.text = ago
        }


        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): ChatRoomViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
                return ChatRoomViewHolder(view, onClickListener)
            }
        }
    }

}