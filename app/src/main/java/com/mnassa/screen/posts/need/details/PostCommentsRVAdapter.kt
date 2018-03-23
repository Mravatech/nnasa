package com.mnassa.screen.posts.need.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.CommentReplyModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter

/**
 * Created by Peter on 3/23/2018.
 */
class PostCommentsRVAdapter : BasePaginationRVAdapter<CommentModel>(), View.OnClickListener {
    var onBindHeader = { header: View -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<CommentModel> = when (viewType) {
        TYPE_COMMENT -> CommentViewHolder.newInstanceComment(parent, this)
        TYPE_COMMENT_REPLY -> CommentViewHolder.newInstanceReply(parent, this)
        else -> throw IllegalArgumentException("Illegal view type $viewType")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<CommentModel> {
        return if (viewType == TYPE_HEADER) HeaderViewHolder.newInstance(parent) else
            super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<CommentModel>, position: Int) {
        if (holder is HeaderViewHolder) onBindHeader(holder.itemView)
        else super.onBindViewHolder(holder, position)
    }

    override fun getViewType(position: Int): Int = when (dataStorage[position]) {
        is CommentReplyModel -> TYPE_COMMENT_REPLY
        else -> TYPE_COMMENT
    }

    override fun onClick(view: View) {

    }

    companion object {
        private const val TYPE_COMMENT = 1
        private const val TYPE_COMMENT_REPLY = 2
    }

    private class CommentViewHolder(itemView: View, onClickListener: View.OnClickListener) : BaseVH<CommentModel>(itemView) {
        override fun bind(item: CommentModel) {

        }

        companion object {
            fun newInstanceComment(parent: ViewGroup, onClickListener: View.OnClickListener): CommentViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
                return CommentViewHolder(view, onClickListener)
            }

            fun newInstanceReply(parent: ViewGroup, onClickListener: View.OnClickListener): CommentViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_reply, parent, false)
                return CommentViewHolder(view, onClickListener)
            }
        }
    }


    private class HeaderViewHolder(itemView: View) : BaseVH<Any>(itemView) {
        override fun bind(item: Any) = Unit

        companion object {
            fun newInstance(parent: ViewGroup): HeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_need_details_header, parent, false)
                return HeaderViewHolder(view)
            }
        }
    }
}

