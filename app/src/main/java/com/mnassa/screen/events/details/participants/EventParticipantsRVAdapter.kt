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
    var onSearchClickListener = {}
    var onCheckParticipantsClickListener = {}
    override val itemsComparator: (item1: EventParticipantItem, item2: EventParticipantItem) -> Int = { first, second ->
        first.compareTo(second)
    }
    override val itemClass: Class<EventParticipantItem> = EventParticipantItem::class.java

    override var filterPredicate: (item: EventParticipantItem) -> Boolean = {
        when (it) {
            is EventParticipantItem.User -> {
                it.user.formattedName.toLowerCase().contains(searchPhrase.toLowerCase())
            }
            is EventParticipantItem.Guest -> {
                it.parent.user.formattedName.toLowerCase().contains(searchPhrase.toLowerCase())
            }
            else -> false
        }
    }

    init {
        itemsTheSameComparator = { first, second ->
            if (first is EventParticipantItem.User && second is EventParticipantItem.User) {
                first.user.id == second.user.id
            } else first == second
        }
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = FilteredSortedDataStorage(filterPredicate, SortedDataStorage(itemClass, this))
        searchListener = dataStorage as SearchListener<EventParticipantItem>
    }

    fun searchByName(text: String) {
        searchPhrase = text
        searchListener.search()
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rlClickableRoot -> onParticipantClickListener(getDataItemByAdapterPosition(position) as EventParticipantItem.User)
            R.id.ivCheckParticipants -> onCheckParticipantsClickListener()
            R.id.ivSearch -> onSearchClickListener()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<EventParticipantItem> {
        return when (viewType) {
            TYPE_USER -> UserViewHolder.newInstance(parent, this)
            TYPE_CONNECTION_HEADER -> HeaderViewHolder.newInstance(parent, this)
            TYPE_OTHER_HEADER -> HeaderViewHolder.newInstance(parent, this)
            else -> throw IllegalStateException()
        }
    }

    override fun getViewType(position: Int): Int = when (dataStorage[position]) {
        is EventParticipantItem.User -> TYPE_USER
        is EventParticipantItem.ConnectionsHeader -> TYPE_CONNECTION_HEADER
        is EventParticipantItem.OtherHeader -> TYPE_OTHER_HEADER
        else -> throw IllegalArgumentException("Invalid type ${dataStorage[position]}")
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
            with(itemView) {
                tvHeader.text = when (item) {
                    is EventParticipantItem.OtherHeader -> fromDictionary(R.string.event_participant_header_other)
                    is EventParticipantItem.ConnectionsHeader -> fromDictionary(R.string.event_participant_header_connection)
                    else -> throw IllegalStateException()
                }
                ivCheckParticipants.isInvisible = !(item is EventParticipantItem.ConnectionsHeader && item.canEdit)
                ivCheckParticipants.isEnabled = (item is EventParticipantItem.ConnectionsHeader && item.canEdit)
                ivSearch.isInvisible = item is EventParticipantItem.OtherHeader
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): HeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_participants_header, parent, false)
                val viewHolder = HeaderViewHolder(view)
                view.ivCheckParticipants.setOnClickListener(onClickListener)
                view.ivCheckParticipants.tag = viewHolder
                view.ivSearch.setOnClickListener(onClickListener)
                view.ivSearch.tag = viewHolder
                return viewHolder
            }
        }
    }
}