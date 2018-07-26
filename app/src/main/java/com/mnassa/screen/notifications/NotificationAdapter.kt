package com.mnassa.screen.notifications

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.NotificationModel
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.notifications.viewholder.NotificationHeaderHolder
import com.mnassa.screen.notifications.viewholder.NotificationHolder
import java.io.Serializable

class NotificationAdapter : BaseSortedPaginationRVAdapter<NotificationAdapter.NotificationItem>(), View.OnClickListener {

    var onItemClickListener = { item: NotificationModel -> }
    override val itemsComparator: (item1: NotificationAdapter.NotificationItem, item2: NotificationAdapter.NotificationItem) -> Int = { first, second ->
        first.compareTo(second)
    }

    override val itemClass: Class<NotificationAdapter.NotificationItem> = NotificationAdapter.NotificationItem::class.java

    init {
        itemsTheSameComparator = { first, second -> first.key == second.key }
        contentTheSameComparator = { first, second ->
            first == second
        }
        dataStorage = SortedDataStorage(itemClass, this)
    }

    fun addNotifications(notifications: List<NotificationModel>) {
        var hasOld = false
        var hasNew = false
        val itemsToAdd = notifications.mapTo(ArrayList<NotificationItem>(notifications.size + 2)) {
            hasNew = hasNew or !it.isOld
            hasOld = hasOld or it.isOld
            NotificationItem.ContentItem(it)
        }
        if (hasOld) {
            itemsToAdd.add(NotificationItem.HeaderItem(isNew = false))
        }
        if (hasNew) {
            itemsToAdd.add(NotificationItem.HeaderItem(isNew = true))
        }
        add(itemsToAdd)
    }

    fun removeNotifications(notifications: List<NotificationModel>) {
        dataStorage.removeAll(notifications.map { NotificationItem.ContentItem(it) })
        recyclerView.invoke {
            it.postDelayed({
                val hasNew = dataStorage.any { it is NotificationItem.ContentItem && !it.content.isOld }
                val hasOld = dataStorage.any { it is NotificationItem.ContentItem && it.content.isOld }
                if (!hasOld) {
                    dataStorage.remove(NotificationItem.HeaderItem(isNew = false))
                }
                if (!hasNew) {
                    dataStorage.remove(NotificationItem.HeaderItem(isNew = true))
                }
            }, 100)
        }
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.llNotificationRoot -> {
                val content = (getDataItemByAdapterPosition(position) as NotificationItem.ContentItem).content
                onItemClickListener(content)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<NotificationAdapter.NotificationItem> {
        return when (viewType) {
            TYPE_HEADER -> NotificationHeaderHolder.newInstance(parent)
            TYPE_CONTENT -> NotificationHolder.newInstance(parent, this)
            else -> throw IllegalArgumentException("Illegal view type $viewType")
        }
    }

    override fun getViewType(position: Int): Int {
        return when (dataStorage[position]) {
            is NotificationItem.ContentItem -> TYPE_CONTENT
            is NotificationItem.HeaderItem -> TYPE_HEADER
        }
    }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_CONTENT = 2

    }

    sealed class NotificationItem : Serializable, Comparable<NotificationItem> {

        data class ContentItem(val content: NotificationModel) : NotificationItem() {
            override val key: String get() = "${!content.isOld}_${content.id}"
        }

        data class HeaderItem(val isNew: Boolean) : NotificationItem() {
            override val key: String get() = "${isNew}_Z"
        }

        abstract val key: String

        override fun compareTo(other: NotificationItem): Int = key.compareTo(other.key) * -1
        override fun equals(other: Any?): Boolean = other is NotificationItem && key == other.key
        override fun hashCode(): Int = key.hashCode()
    }

}