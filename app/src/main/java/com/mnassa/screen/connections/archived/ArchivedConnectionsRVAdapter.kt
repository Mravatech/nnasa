package com.mnassa.screen.connections.archived

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter

/**
 * Created by Peter on 9.03.2018.
 */
class ArchivedConnectionsRVAdapter : BasePaginationRVAdapter<DeclinedShortAccountModel>(), View.OnClickListener {

    override fun onClick(v: View) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<DeclinedShortAccountModel> {
        return ArchivedConnectionItemViewHolder.newInstance(parent, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<DeclinedShortAccountModel> {
        if (viewType == TYPE_HEADER) {
            return ArchivedConnectionHeaderViewHolder.newInstance(parent)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    private class ArchivedConnectionItemViewHolder(itemView: View, private val onClickListener: View.OnClickListener) :
            BaseVH<DeclinedShortAccountModel>(itemView) {

        override fun bind(item: DeclinedShortAccountModel) {

        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): ArchivedConnectionItemViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_archived_item, parent, false)
                return ArchivedConnectionItemViewHolder(view, onClickListener)
            }
        }
    }

    private class ArchivedConnectionHeaderViewHolder(itemView: View) :
            BaseVH<DeclinedShortAccountModel>(itemView) {

        override fun bind(item: DeclinedShortAccountModel) {

        }

        companion object {
            fun newInstance(parent: ViewGroup): ArchivedConnectionHeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection_archived_header, parent, false)
                return ArchivedConnectionHeaderViewHolder(view)
            }
        }
    }

    private companion object {
        const val TYPE_ITEM = 1
    }
}