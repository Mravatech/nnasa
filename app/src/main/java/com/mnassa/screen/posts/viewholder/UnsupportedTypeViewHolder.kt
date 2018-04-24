package com.mnassa.screen.posts.viewholder

import android.view.View
import android.view.ViewGroup
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter

/**
 * Created by Peter on 4/24/2018.
 */
class UnsupportedTypeViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PostModel>(itemView) {
    override fun bind(item: PostModel) {
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): UnsupportedTypeViewHolder {
            val view = View(parent.context)
            view.visibility = View.GONE
            return UnsupportedTypeViewHolder(view, onClickListener)
        }
    }
}