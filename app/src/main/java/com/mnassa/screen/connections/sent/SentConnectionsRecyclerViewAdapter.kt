package com.mnassa.screen.connections.sent

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connections_sent.view.*

/**
 * Created by Peter on 11.03.2018.
 */
class SentConnectionsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

    var onCancelClickListener = { account: ShortAccountModel -> }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnCancel -> {
                val vh = view.tag as RecyclerView.ViewHolder
                val position = vh.adapterPosition
                if (position >= 0) {
                    onCancelClickListener(getDataItemByAdapterPosition(position))
                }
            }
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

                tvPosition.text = item.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvPosition.goneIfEmpty()

                btnCancel.setOnClickListener(onClickListener)
                btnCancel.tag = this@SentConnectionViewHolder
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