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

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationAdapter : BaseSortedPaginationRVAdapter<NotificationModel>(), View.OnClickListener {

    var onItemClickListener = { item: NotificationModel -> }
    override val itemsComparator: (item1: NotificationModel, item2: NotificationModel) -> Int = { first, second ->
        when {
            itemsTheSameComparator(first, second) -> 0
            first.type == NEW && second.type != NEW -> -1
            first.type != NEW && second.type == NEW -> 1
            first.type == OLD && second.isOld -> -1
            first.isOld && second.type == OLD -> 1
            first.type == OLD && !second.isOld -> 1
            !first.isOld && second.type == OLD -> -1
            !first.isOld && second.isOld -> -1
            first.isOld && !second.isOld -> 1
            else -> first.createdAt.compareTo(second.createdAt) * -1
        }
    }

    override val itemClass: Class<NotificationModel> = NotificationModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second ->
            first == second
        }
        dataStorage = SortedDataStorage<NotificationModel>(itemClass, this)
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.llNotificationRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<NotificationModel> {
        return when (viewType) {
            HEADER -> NotificationHeaderHolder.newInstance(parent)
            CONTENT -> NotificationHolder.newInstance(parent, this)
            else -> throw IllegalArgumentException("Illegal view type $viewType")
        }
    }

    override fun getViewType(position: Int): Int {
        val type = dataStorage[position].type
        return when (type) {
            NEW, OLD -> HEADER
            else -> CONTENT
        }
    }

    companion object {
        private const val HEADER = 1
        private const val CONTENT = 2

        const val NEW = "NEW"
        const val OLD = "OLD"
    }

}