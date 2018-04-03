package com.mnassa.screen.posts.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter

/**
 * Created by Peter on 3/14/2018.
 */
class ProfileViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {
    override fun bind(item: PostModel) {
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): ProfileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_feed_profile, parent, false)
            return ProfileViewHolder(view, onClickListener)
        }
    }
}