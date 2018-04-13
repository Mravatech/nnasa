package com.mnassa.screen.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.model.impl.NotificationModelImpl
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.notifications.viewholder.NotificationHeaderHolder
import com.mnassa.screen.notifications.viewholder.NotificationHolder
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationAdapter : BaseSortedPaginationRVAdapter<NotificationModel>(), View.OnClickListener {

    override val itemsComparator: (item1: NotificationModel, item2: NotificationModel) -> Int = { first, second ->
        when {
            itemsTheSameComparator(first, second) -> 0
            first.type == NEW -> -1
            first.type == OLD -> if (second.isOld) -1 else 1
            else -> first.createdAt.compareTo(second.createdAt) * -1
        }
    }

    override val itemClass: Class<NotificationModel> = NotificationModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.extra.id == second.extra.id && first.createdAt == second.createdAt }
        contentTheSameComparator = { first, second ->
            first == second
        }
        dataStorage = NotificationsDataStorage(this)
    }

    override fun onClick(v: View?) {

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

    class NotificationsDataStorage(private val adapter: BaseSortedPaginationRVAdapter<NotificationModel>) :
            SortedDataStorage<NotificationModel>(NotificationModel::class.java, adapter), DataStorage<NotificationModel> {
        private var headerOld: NotificationModel? = null
        private var headerNew: NotificationModel? = null

        override fun addAll(elements: Collection<NotificationModel>): Boolean {
            adapter.postUpdate {
                wrappedList.beginBatchedUpdates()
                elements.forEach {
                    if (it.isOld && headerOld == null) {
                        headerOld = getNotificationHeader(it)
                    }
                    if (!it.isOld && headerNew == null) {
                        headerNew = getNotificationHeader(it)
                    }
                    if (headerNew != null && wrappedList.indexOf(headerNew) == -1) {
                        super.add(requireNotNull(headerNew))
                    }
                    if (headerOld != null && wrappedList.indexOf(headerOld) == -1) {
                        super.add(requireNotNull(headerOld))
                    }
                }
                super.addAll(elements)
                wrappedList.endBatchedUpdates()
            }
            return true
        }

        private fun getNotificationHeader(model: NotificationModel): NotificationModel {
            val date = Date(model.createdAt.time - 1)
            val type = if (model.isOld) OLD else NEW
            return NotificationModelImpl(
                    createdAt = date,
                    text = "",
                    type = type,
                    extra = model.extra,
                    isOld = model.isOld
            )
        }
    }

    companion object {
        private const val HEADER = 1
        private const val CONTENT = 2

        private const val NEW = "NEW"
        private const val OLD = "OLD"
    }

}