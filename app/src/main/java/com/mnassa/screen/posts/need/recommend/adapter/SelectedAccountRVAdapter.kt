package com.mnassa.screen.posts.need.recommend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.avatarRound
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_selected_account.view.*

/**
 * Created by Peter on 3/27/2018.
 */
class SelectedAccountRVAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return AccountViewHolder.newInstance(parent, this)
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position >= 0) {
            dataStorage.remove(getDataItemByAdapterPosition(position))
        }
    }

    private class AccountViewHolder(itemView: View) : BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) {
            itemView.ivAvatar.avatarRound(item.avatar)
        }

        companion object {

            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): AccountViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_account, parent, false)
                val viewHolder = AccountViewHolder(view)
                view.ivCancelSelection.setOnClickListener(onClickListener)
                view.ivCancelSelection.tag = viewHolder
                return viewHolder
            }
        }
    }
}