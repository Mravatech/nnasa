package com.mnassa.screen.posts.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_news_feed_need.view.*

/**
 * Created by Peter on 3/14/2018.
 */
class NeedViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<Post>(itemView) {
    override fun bind(item: Post) {
        with(itemView) {
            ivAvatar.avatarRound(item.author.avatar)
            tvUserName.text = item.author.formattedName
//            tvTime.text = item.createdAt
            tvDescription.text = item.text
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NeedViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_feed_need, parent, false)
            return NeedViewHolder(view, onClickListener)
        }
    }
}