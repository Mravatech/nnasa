package com.mnassa.screen.connections.sent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedFromEvent
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_connections_sent.view.*

/**
 * Created by Peter on 11.03.2018.
 */
class SentConnectionsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

    var onCancelClickListener = { account: ShortAccountModel -> }
    var onItemClickListener = { account: ShortAccountModel -> }

    fun destoryCallbacks() {
        onCancelClickListener = { }
        onItemClickListener = { }
    }

    override fun onClick(view: View) {
        val vh = view.tag as RecyclerView.ViewHolder
        val position = vh.adapterPosition
        if (position < 0) return

        when (view.id) {
            R.id.btnCancel -> onCancelClickListener(getDataItemByAdapterPosition(position))
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return SentConnectionViewHolder.newInstance(parent, this)
    }

    private class SentConnectionViewHolder(itemView: View, val onClickListener: View.OnClickListener) : BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedPosition
                tvPosition.goneIfEmpty()

                tvEventName.text = item.formattedFromEvent
                tvEventName.goneIfEmpty()

                btnCancel.setOnClickListener(onClickListener)
                btnCancel.tag = this@SentConnectionViewHolder

                rlClickableRoot.setOnClickListener(onClickListener)
                rlClickableRoot.tag = this@SentConnectionViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): SentConnectionViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_sent, parent, false)
                return SentConnectionViewHolder(view, onClickListener)
            }
        }
    }
}