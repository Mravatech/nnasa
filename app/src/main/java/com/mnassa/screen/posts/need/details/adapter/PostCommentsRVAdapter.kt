package com.mnassa.screen.posts.need.details.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.CommentReplyModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.extensions.toTimeAgo
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

    private class CommentViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BaseVH<CommentModel>(itemView) {

        override fun bind(item: CommentModel) {
            val avatar = itemView.findViewById<ImageView>(R.id.ivAvatar)
            val userName = itemView.findViewById<TextView>(R.id.tvUserName)
            val comment = itemView.findViewById<TextView>(R.id.tvCommentText)
            val recommendedAccounts = itemView.findViewById<RecyclerView>(R.id.rvRecommendedAccounts)
            val creationTime = itemView.findViewById<TextView>(R.id.tvCreationTime)
            val replyButton = itemView.findViewById<Button>(R.id.btnReply)

            itemView.findViewById<View?>(R.id.vSeparator)?.visibility = if (adapterPosition <= FIRST_ITEM_POSITION) View.INVISIBLE else View.VISIBLE

            avatar.avatarRound(item.creator.avatar)
            userName.text = item.creator.formattedName
            comment.text = item.text
            comment.goneIfEmpty()

            //TODO
            recommendedAccounts.visibility = if (item.recommends.isEmpty()) View.GONE else View.VISIBLE

            creationTime.text = item.createdAt.toTimeAgo()

            replyButton.tag = this
            replyButton.setOnClickListener(onClickListener)
        }

        companion object {
            private const val FIRST_ITEM_POSITION = 1

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

