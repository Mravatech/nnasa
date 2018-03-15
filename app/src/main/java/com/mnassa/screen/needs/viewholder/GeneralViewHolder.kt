package com.mnassa.screen.needs.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.NewsFeedItemModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter

/**
 * Created by Peter on 3/14/2018.
 */
class GeneralViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<NewsFeedItemModel>(itemView) {
    override fun bind(item: NewsFeedItemModel) {
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): GeneralViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_feed_general, parent, false)
            return GeneralViewHolder(view, onClickListener)
        }
    }
}