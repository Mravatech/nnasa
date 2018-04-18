package com.mnassa.screen.events

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventStatus
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.isActive
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import kotlinx.android.synthetic.main.event_date.view.*
import kotlinx.android.synthetic.main.item_event.view.*
import java.text.SimpleDateFormat

/**
 * Created by Peter on 4/16/2018.
 */
class EventsRVAdapter(private val languageProvider: LanguageProvider) : BaseSortedPaginationRVAdapter<EventModel>(), View.OnClickListener {
    override val itemsComparator: (item1: EventModel, item2: EventModel) -> Int = { item1, item2 ->
        item1.createdAt.compareTo(item2.createdAt) * -1
    }
    override val itemClass: Class<EventModel> = EventModel::class.java
    var onItemClickListener = { item: EventModel -> }
    var onAuthorClickListener = { item: EventModel -> }
    var onAttachedToWindow: (item: EventModel) -> Unit = { }
    var onDetachedFromWindow: (item: EventModel) -> Unit = { }

    init {
        dataStorage = SortedDataStorage(itemClass, this)
    }

    fun destroyCallbacks() {
        onItemClickListener = { item: EventModel -> }
        onAuthorClickListener = { item: EventModel -> }
        onAttachedToWindow = { }
        onDetachedFromWindow = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<EventModel> {
        return EventViewHolder.newInstance(parent, this, languageProvider)
    }

    override fun onViewAttachedToWindow(holder: BaseVH<EventModel>) {
        super.onViewAttachedToWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition)
        if (position >= 0) {
            onAttachedToWindow(dataStorage[position])
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseVH<EventModel>) {
        super.onViewDetachedFromWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition)
        if (position >= 0) {
            onDetachedFromWindow(dataStorage[position])
        }
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return

        when (view.id) {
            R.id.rlAuthorRoot -> onAuthorClickListener(getDataItemByAdapterPosition(position))
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
        }
    }

    fun saveState(outState: Bundle) {
        outState.putSerializable(EXTRA_STATE_EVENTS, dataStorage.toCollection(ArrayList()))
    }

    @Suppress("UNCHECKED_CAST")
    fun restoreState(inState: Bundle) {
        dataStorage.set(inState.getSerializable(EXTRA_STATE_EVENTS) as List<EventModel>)
    }

    class EventViewHolder(itemView: View, languageProvider: LanguageProvider) : BaseVH<EventModel>(itemView) {
        private val dateFormatter = SimpleDateFormat("dd MMM", languageProvider.locale)
        private val dayOfWeekFormatter = SimpleDateFormat("EEE", languageProvider.locale)

        override fun bind(item: EventModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.author.avatar)
                tvUserName.text = item.author.formattedName
                tvTime.text = item.createdAt.toTimeAgo()

                ivEvent.image(item.pictures.firstOrNull())

                tvEventTitle.text = item.title
                tvEventDescription.text = item.text

                tvDate.text = dateFormatter.format(item.startAt)
                tvDayOfWeek.text = dayOfWeekFormatter.format(item.startAt)

                flEventDisabled.isInvisible = item.isActive
                if (item.isActive) ivEvent.enable() else ivEvent.disable()
                val imgSrc = when (item.status) {
                    is EventStatus.ANNULED -> R.drawable.cancelled
                    is EventStatus.CLOSED -> R.drawable.finished
                    is EventStatus.SUSPENDED -> R.drawable.sold_out
                    else -> 0
                }
                ivEventStatus.setImageResource(imgSrc)
                tvEventType.text = item.formattedType
            }

        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, languageProvider: LanguageProvider): EventViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
                val viewHolder = EventViewHolder(view, languageProvider)

                with(view) {
                    rlAuthorRoot.setOnClickListener(onClickListener)
                    rlAuthorRoot.tag = viewHolder

                    rlClickableRoot.setOnClickListener(onClickListener)
                    rlClickableRoot.tag = viewHolder
                }

                return viewHolder
            }
        }
    }

    companion object {
        private const val EXTRA_STATE_EVENTS = "EXTRA_STATE_EVENTS"
    }
}