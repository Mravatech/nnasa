package com.mnassa.screen.posts.need.details.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_post_tag.view.*

/**
 * Created by Peter on 3/20/2018.
 */
class PostTagRVAdapter : BasePaginationRVAdapter<TagModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<TagModel> {
        return TagViewHolder.newInstance(parent)
    }

    class TagViewHolder(itemView: View) : BaseVH<TagModel>(itemView) {

        override fun bind(item: TagModel) {
            with(itemView) {
//                tvTagName.text = item.name.toString()
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup): TagViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_tag, parent, false)
                return TagViewHolder(view)
            }
        }
    }
}