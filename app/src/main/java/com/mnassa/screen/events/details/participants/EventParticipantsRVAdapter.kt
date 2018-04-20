package com.mnassa.screen.events.details.participants

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_event_participants.view.*
import kotlinx.android.synthetic.main.item_event_participants_header.view.*

/**
 * Created by Peter on 19.04.2018.
 */
class EventParticipantsRVAdapter : BaseSortedPaginationRVAdapter<EventParticipantItem>(), View.OnClickListener {
    var onParticipantClickListener = { user: EventParticipantItem.User -> }
    override val itemsComparator: (item1: EventParticipantItem, item2: EventParticipantItem) -> Int = { first, second ->
        when {
            first is EventParticipantItem.User && second is EventParticipantItem.User -> {
                when {
                    first.isInConnections == second.isInConnections -> first.user.id.compareTo(second.user.id)
                    first.isInConnections -> -1
                    second.isInConnections -> 1
                    else -> throw IllegalStateException()
                }
            }
            first !is EventParticipantItem.User && second !is EventParticipantItem.User -> {
                if (first == EventParticipantItem.ConnectionsHeader) -1 else 1
            }
            first == EventParticipantItem.ConnectionsHeader -> -1
            first == EventParticipantItem.OtherHeader -> if ((second as EventParticipantItem.User).isInConnections) 1 else -1
            second == EventParticipantItem.ConnectionsHeader -> 1
            second == EventParticipantItem.OtherHeader -> if ((first as EventParticipantItem.User).isInConnections) -1 else 1

            else -> throw IllegalStateException()
        }
    }
    override val itemClass: Class<EventParticipantItem> = EventParticipantItem::class.java

    init {
        dataStorage = SortedDataStorage(itemClass, this)
        itemsTheSameComparator = { first, second ->
            if (first is EventParticipantItem.User && second is EventParticipantItem.User) {
                first.user.id == second.user.id
            } else first == second
        }
        contentTheSameComparator = { first, second -> first == second }
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rlClickableRoot -> onParticipantClickListener(getDataItemByAdapterPosition(position) as EventParticipantItem.User)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<EventParticipantItem> {
        return when (viewType) {
            TYPE_USER -> UserViewHolder.newInstance(parent, this)
            TYPE_CONNECTION_HEADER -> HeaderViewHolder.newInstance(parent)
            TYPE_OTHER_HEADER -> HeaderViewHolder.newInstance(parent)
            else -> throw IllegalStateException()
        }
    }

    override fun getViewType(position: Int): Int = when (dataStorage[position]) {
        is EventParticipantItem.User -> TYPE_USER
        is EventParticipantItem.ConnectionsHeader -> TYPE_CONNECTION_HEADER
        is EventParticipantItem.OtherHeader -> TYPE_OTHER_HEADER
    }

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_CONNECTION_HEADER = 2
        private const val TYPE_OTHER_HEADER = 3
    }

    private class UserViewHolder(itemView: View) : BaseVH<EventParticipantItem>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun bind(item: EventParticipantItem) {
            item as EventParticipantItem.User
            with(itemView) {
                ivAvatar.avatarRound(item.user.avatar)
                tvUserName.text = item.user.formattedName
                tvPosition.text = item.user.formattedPosition
                tvPosition.goneIfEmpty()
                tvEventName.text = item.user.formattedFromEvent
                tvEventName.goneIfEmpty()

                tvGuestsCount.text = "+${item.guestsCount}"
                tvGuestsCount.isInvisible = item.guestsCount <= 0
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_participants, parent, false)
                val viewHolder = UserViewHolder(view)
                with(view) {
                    rlClickableRoot.tag = viewHolder
                    rlClickableRoot.setOnClickListener(onClickListener)
                }
                return viewHolder
            }
        }
    }

    private class HeaderViewHolder(itemView: View) : BaseVH<EventParticipantItem>(itemView) {
        override fun bind(item: EventParticipantItem) {
            itemView.tvHeader.text = when (item) {
                is EventParticipantItem.OtherHeader -> fromDictionary(R.string.event_participant_header_other)
                is EventParticipantItem.ConnectionsHeader -> fromDictionary(R.string.event_participant_header_connection)
                else -> throw IllegalStateException()
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup): HeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_participants_header, parent, false)
                return HeaderViewHolder(view)
            }
        }
    }
}