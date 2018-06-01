package com.mnassa.screen.posts.need.details.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_comment_recommended_account.view.*

/**
 * Created by Peter on 3/28/2018.
 */
class RecommendedAccountsRVAdapter : BasePaginationRVAdapter<ShortAccountModel>() {

    var onItemClickListener: View.OnClickListener = View.OnClickListener { }

    fun destroyCallbacks() {
        onItemClickListener = View.OnClickListener { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return RecommendedAccountViewHolder.newInstance(parent, onItemClickListener)
    }

    private class RecommendedAccountViewHolder(itemView: View) : BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName
                tvUserPosition.text = item.formattedPosition
                tvUserPosition.goneIfEmpty()

                rlClickableRoot.tag = item
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): RecommendedAccountViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_recommended_account, parent, false)
                val viewHolder = RecommendedAccountViewHolder(view)
                view.rlClickableRoot.setOnClickListener(onClickListener)
                return viewHolder
            }
        }
    }
}