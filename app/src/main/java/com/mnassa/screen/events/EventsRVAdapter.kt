package com.mnassa.screen.events

import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mnassa.R
import com.mnassa.core.addons.asReference
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.event_date.view.*
import kotlinx.android.synthetic.main.item_event.view.*

/**
 * Created by Peter on 4/16/2018.
 */
class EventsRVAdapter(private val userProfileInteractor: UserProfileInteractor) : BaseSortedPaginationRVAdapter<EventModel>(), View.OnClickListener {
    override val itemsComparator: (item1: EventModel, item2: EventModel) -> Int = { item1, item2 ->
        item1.createdAt.compareTo(item2.createdAt) * -1
    }
    override val itemClass: Class<EventModel> = EventModel::class.java
    var onItemClickListener = { item: EventModel -> }
    var onAuthorClickListener = { item: EventModel -> }
    var onGroupClickListener = { group: GroupModel -> }
    var onAttachedToWindow: (item: EventModel) -> Unit = { }
    var onDetachedFromWindow: (item: EventModel) -> Unit = { }

    init {
        dataStorage = SortedDataStorage(itemClass, this)
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second -> first == second }
    }

    fun destroyCallbacks() {
        onItemClickListener = { item: EventModel -> }
        onAuthorClickListener = { item: EventModel -> }
        onGroupClickListener = { }
        onAttachedToWindow = { }
        onDetachedFromWindow = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<EventModel> {
        return EventViewHolder.newInstance(parent, this, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<EventModel>, position: Int) {
        if (holder is EventViewHolder) {
            holder.currentAccountId = userProfileInteractor.getAccountIdOrNull()
        }
        super.onBindViewHolder(holder, position)
    }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        return if (item.groups.isEmpty()) TYPE_EVENT_SIMPLE else TYPE_EVENT_COMMUNITY
    }

    override fun onViewAttachedToWindow(holder: BaseVH<EventModel>) {
        super.onViewAttachedToWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition).takeIf { it >= 0 } ?: return
        val dataItem = dataStorage[position]
        onAttachedToWindow(dataItem)
        holder.itemView.findViewById<TextView?>(R.id.tvTime)?.let {
            it.startUpdateTimeJob(dataItem.originalCreatedAt)
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseVH<EventModel>) {
        super.onViewDetachedFromWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition).takeIf { it >= 0 } ?: return
        onDetachedFromWindow(dataStorage[position])
        holder.itemView.findViewById<TextView?>(R.id.tvTime)?.let {
            it.stopUpdateTimeJob()
        }
    }

    override fun onClick(view: View) {
        val tag = view.tag
        if (tag is GroupModel) {
            onGroupClickListener(tag)
            return
        }

        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return

        when (view.id) {
            R.id.rlAuthorRoot -> onAuthorClickListener(getDataItemByAdapterPosition(position))
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
        }
    }

    class EventViewHolder(itemView: View,
                          private val onClickListener: View.OnClickListener) : BaseVH<EventModel>(itemView) {

        var currentAccountId: String? = null

        override fun bind(item: EventModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.author.avatar)
                tvUserName.text = item.author.formattedName
                tvTime.text = item.createdAt.toTimeAgo()

                ivEvent.image(item.pictures.firstOrNull())

                tvEventTitle.text = item.title
                tvEventDescription.text = item.text

                item.bindDate(llEventDateRoot)

                var imgSrc = when (item.status) {
                    is EventStatus.ANNULED -> R.drawable.cancelled
                    is EventStatus.CLOSED -> R.drawable.finished
                    is EventStatus.SUSPENDED -> R.drawable.sold_out
                    else -> 0
                }
                if (item.ticketsSold >= item.ticketsTotal) {
                    imgSrc = R.drawable.sold_out
                }
                ivEventStatus.setImageResource(imgSrc)
                flEventDisabled.isInvisible = imgSrc == 0
                if (imgSrc == 0) ivEvent.enable() else ivEvent.disable()

                tvEventType.text = item.formattedType
                ivIsTicketsBought.isInvisible = !item.participants.contains(currentAccountId)
            }
            bindGroup(item)
        }

        private fun bindGroup(item: EventModel) {
            if (item.groups.isEmpty()) return

            with(itemView) {
                val tvGroupText: TextView = findViewById(R.id.tvGroupText)

                val groupSpan = SpannableStringBuilder(fromDictionary(R.string.need_item_from_group))
                groupSpan.append(" ")
                var startSpan = groupSpan.length

                val tvGroupTextReference = tvGroupText.asReference()
                item.groups.forEachIndexed { index, group ->
                    groupSpan.append(group.formattedName)
                    groupSpan.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View?) {
                            tvGroupTextReference.invoke {
                                this.tag = group
                                onClickListener.onClick(this)
                            }
                        }
                    }, startSpan, groupSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    if (index != (item.groups.size - 1)) {
                        groupSpan.append(", ")
                    }

                    startSpan = groupSpan.length
                }

                tvGroupText.text = groupSpan
                tvGroupText.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup,
                            onClickListener: View.OnClickListener,
                            type: Int): EventViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
                val viewHolder = EventViewHolder(view, onClickListener)

                with(view) {
                    rlAuthorRoot.setOnClickListener(onClickListener)
                    rlAuthorRoot.tag = viewHolder

                    rlClickableRoot.setOnClickListener(onClickListener)
                    rlClickableRoot.tag = viewHolder

                    rlGroupRoot.isGone = type == TYPE_EVENT_SIMPLE
                }

                return viewHolder
            }
        }
    }

    companion object {
        private const val TYPE_EVENT_SIMPLE = 1
        private const val TYPE_EVENT_COMMUNITY = 2

    }
}