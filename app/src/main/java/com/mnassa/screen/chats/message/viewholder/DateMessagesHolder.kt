package com.mnassa.screen.chats.message.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.extensions.isTheSameDay
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_chat_date.view.*
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/5/2018
 */

class DateMessagesHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<ChatMessageModel>(itemView) {
    val date: Date = Date()
    override fun bind(item: ChatMessageModel) {
        if (date.isTheSameDay(item.createdAt)) {
            itemView.tvDateOfMessageGroup.text = fromDictionary(R.string.chats_today_message)
            return
        }
        itemView.tvDateOfMessageGroup.text = getDateByTimeMillis(item.createdAt.time)
    }

    private fun getDateByTimeMillis(createdAt: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    companion object {
        fun newInstance(parent: ViewGroup): DateMessagesHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_date, parent, false)
            return DateMessagesHolder(view)
        }
    }

}