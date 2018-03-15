package com.mnassa.screen.needs.viewholder

import android.view.View
import android.view.ViewParent
import com.mnassa.domain.model.NewsFeedItem
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter

/**
 * Created by Peter on 3/14/2018.
 */
class ProfileViewHolder(itemView: View) : BasePaginationRVAdapter.BaseVH<NewsFeedItem>(itemView) {
    override fun bind(item: NewsFeedItem) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun newInstance(parent: ViewParent, onClickListener: View.OnClickListener): ProfileViewHolder {
            TODO()
        }
    }
}