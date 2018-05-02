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
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.comments.CommentsRewardModel
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 3/23/2018.
 */

class PostCommentsRVAdapter(private val commentsRewardModel: CommentsRewardModel, private val headerInflater: (parent: ViewGroup) -> View) : BaseSortedPaginationRVAdapter<CommentModel>(), View.OnClickListener {
    var onBindHeader = { header: View -> }
    var onReplyClick = { comment: CommentModel -> }
    var onCommentOptionsClick = { view: View, comment: CommentModel -> }
    var onCommentUsefulClick = { comment: CommentModel -> }
    var onRecommendedAccountClick = { view: View, account: ShortAccountModel -> }

    fun destroyCallbacks() {
        onBindHeader = { }
        onReplyClick = { }
        onCommentOptionsClick = { _: View, _: CommentModel -> }
        onRecommendedAccountClick = { _: View, _: ShortAccountModel -> }
    }

    override val itemsComparator: (item1: CommentModel, item2: CommentModel) -> Int = { first, second ->
        when {
            itemsTheSameComparator(first, second) -> 0
            first is CommentReplyModel && second is CommentReplyModel -> {
                val parentComparingResult = first.parentId.compareTo(second.parentId)
                if (parentComparingResult == 0) first.id.compareTo(second.id) else parentComparingResult
            }
            first is CommentReplyModel && second !is CommentReplyModel -> {
                val parentComparingResult = first.parentId.compareTo(second.id)
                if (parentComparingResult == 0) 1 else parentComparingResult
            }
            first !is CommentReplyModel && second is CommentReplyModel -> {
                val parentComparingResult = first.id.compareTo(second.parentId)
                if (parentComparingResult == 0) -1 else parentComparingResult
            }
            else -> first.id.compareTo(second.id)
        }
    }

    override val itemClass: Class<CommentModel> = CommentModel::class.java

    init {
        dataStorage = SortedDataStorage(itemClass, this)
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second -> first == second }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<CommentModel> = when (viewType) {
        TYPE_COMMENT -> CommentViewHolder.newInstanceComment(parent, this, commentsRewardModel)
        TYPE_COMMENT_REPLY -> CommentViewHolder.newInstanceReply(parent, this, commentsRewardModel)
        else -> throw IllegalArgumentException("Illegal view type $viewType")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<CommentModel> {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(headerInflater(parent))
            else -> super.onCreateViewHolder(parent, viewType)
        }
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
        val position = (view.tag as? RecyclerView.ViewHolder)?.adapterPosition ?: -1

        when (view.id) {
            R.id.rlClickableRoot -> onRecommendedAccountClick(view, view.tag as ShortAccountModel)
            R.id.btnReply -> if (position >= 0) onReplyClick(getDataItemByAdapterPosition(position))
            R.id.commentRoot -> if (position >= 0) onCommentOptionsClick(view, getDataItemByAdapterPosition(position))
            R.id.btnUseful -> if (position >= 0) onCommentUsefulClick(getDataItemByAdapterPosition(position))
        }
    }

    companion object {
        private const val TYPE_COMMENT = 1
        private const val TYPE_COMMENT_REPLY = 2
    }

    private class CommentViewHolder(itemView: View, private val onClickListener: View.OnClickListener, private val commentsRewardModel: CommentsRewardModel) : BaseVH<CommentModel>(itemView), View.OnLongClickListener {

        override fun bind(item: CommentModel) {
            //used findViewById because this method is identical for Comment and Reply
            val avatar = itemView.findViewById<ImageView>(R.id.ivAvatar)
            val userName = itemView.findViewById<TextView>(R.id.tvUserName)
            val comment = itemView.findViewById<TextView>(R.id.tvCommentText)

            val creationTime = itemView.findViewById<TextView>(R.id.tvCreationTime)
            val replyButton = itemView.findViewById<Button>(R.id.btnReply)
            val usefulButton = itemView.findViewById<Button>(R.id.btnUseful)
            val commentRoot = itemView.findViewById<View>(R.id.commentRoot)

            itemView.findViewById<View?>(R.id.vSeparator)?.isInvisible = adapterPosition <= FIRST_ITEM_POSITION

            avatar.avatarRound(item.creator.avatar)
            userName.text = item.creator.formattedName
            comment.text = item.text
            comment.goneIfEmpty()

            bindRecommendedContactsList(item)

            creationTime.text = item.createdAt.toTimeAgo()

            replyButton.text = fromDictionary(R.string.posts_comment_reply)
            replyButton.tag = this
            replyButton.setOnClickListener(onClickListener)
            usefulButton.text = fromDictionary(R.string.comment_useful)
            usefulButton.tag = this
            usefulButton.setOnClickListener(onClickListener)
            if (commentsRewardModel.canReward) {
                handleUseful(item, usefulButton, commentsRewardModel.isOwner)
            } else {
                usefulButton.visibility = View.INVISIBLE
                usefulButton.isEnabled = false
            }
            commentRoot.tag = this
            commentRoot.setOnLongClickListener(this)
        }

        private fun handleUseful(item: CommentModel, usefulButton: Button, isMyPost: Boolean) {
            when {
                (isMyPost && item.isMyComment()) || (!isMyPost && !item.isRewarded) -> {
                    usefulButton.visibility = View.INVISIBLE
                    usefulButton.isEnabled = false
                }
                (isMyPost && !item.isRewarded) -> {
                    usefulButton.visibility = View.VISIBLE
                    usefulButton.isEnabled = true
                }
                item.isRewarded -> {
                    usefulButton.visibility = View.VISIBLE
                    usefulButton.isEnabled = false
                }
                else -> {
                    usefulButton.visibility = View.INVISIBLE
                    usefulButton.isEnabled = false
                }
            }
        }

        private fun bindRecommendedContactsList(item: CommentModel) {
            val recommendedAccounts = itemView.findViewById<RecyclerView>(R.id.rvRecommendedAccounts)

            recommendedAccounts.isGone = item.recommends.isEmpty()
            recommendedAccounts.isNestedScrollingEnabled = false
            recommendedAccounts.itemAnimator = null

            var adapter: RecommendedAccountsRVAdapter? = recommendedAccounts.adapter as RecommendedAccountsRVAdapter?
            if (adapter == null) {
                adapter = RecommendedAccountsRVAdapter()
                recommendedAccounts.adapter = adapter
            }
            adapter.set(item.recommends)
            adapter.onItemClickListener = onClickListener
        }

        override fun onLongClick(view: View): Boolean {
            onClickListener.onClick(view)
            return true
        }

        companion object {
            private const val FIRST_ITEM_POSITION = 1

            fun newInstanceComment(parent: ViewGroup, onClickListener: View.OnClickListener, commentRewardModel: CommentsRewardModel): CommentViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
                return CommentViewHolder(view, onClickListener, commentRewardModel)
            }

            fun newInstanceReply(parent: ViewGroup, onClickListener: View.OnClickListener, commentRewardModel: CommentsRewardModel): CommentViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_reply, parent, false)
                return CommentViewHolder(view, onClickListener, commentRewardModel)
            }
        }
    }

    private class HeaderViewHolder(itemView: View) : BaseVH<Any>(itemView) {
        override fun bind(item: Any) = Unit
    }
}
