package com.mnassa.screen.posts.need.details.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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

class PostCommentsRVAdapter(private val commentsRewardModel: CommentsRewardModel,
                            private val headerInflater: (parent: ViewGroup) -> View,
                            reverseOrder: Boolean = false) :
        BaseSortedPaginationRVAdapter<CommentModel>(reverseOrder), View.OnClickListener {
    var onBindHeader = { header: View -> }
    var onReplyClick = { comment: CommentModel -> }
    var onCommentOptionsClick = { view: View, comment: CommentModel -> }
    var onCommentUsefulClick = { comment: CommentModel -> }
    var onRecommendedAccountClick = { view: View, account: ShortAccountModel -> }
    var onCommentAuthorClick = { account: ShortAccountModel -> }
    var onImageClickListener = { comment: CommentModel, imagePosition: Int -> }

    fun destroyCallbacks() {
        onBindHeader = { }
        onReplyClick = { }
        onCommentOptionsClick = { _: View, _: CommentModel -> }
        onRecommendedAccountClick = { _: View, _: ShortAccountModel -> }
        onCommentAuthorClick = { }
        onImageClickListener = { comment: CommentModel, imagePosition: Int -> }
    }

    override val itemsComparator: (item1: CommentModel, item2: CommentModel) -> Int = { first, second ->
        val result = when {
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
        result * if (reverseOrder) -1 else 1
    }

    override val itemClass: Class<CommentModel> = CommentModel::class.java

    init {
        dataStorage = SortedDataStorage(itemClass, this)
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second -> first == second }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<CommentModel> {
        return when {
            viewType.hasFlag(TYPE_COMMENT) -> CommentViewHolder.newInstanceComment(
                    parent = parent,
                    onClickListener = this,
                    commentRewardModel = commentsRewardModel,
                    imagesCount = viewType.getImagesCount()
            )
            viewType.hasFlag(TYPE_COMMENT_REPLY) -> CommentViewHolder.newInstanceReply(
                    parent = parent,
                    onClickListener = this,
                    commentRewardModel = commentsRewardModel,
                    imagesCount = viewType.getImagesCount()
            )
            else -> throw IllegalArgumentException("Illegal view type $viewType")
        }
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

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        var type = when (item) {
            is CommentReplyModel -> TYPE_COMMENT_REPLY
            else -> TYPE_COMMENT
        }

        type = type or when (item.images.size) {
            0 -> 0
            1 -> IMAGE_1
            2 -> IMAGE_2
            3 -> IMAGE_3
            else -> IMAGE_MORE
        }

        type = type or when (item.recommends.size) {
            0 -> 0
            1 -> RECOMMEND_1
            2 -> RECOMMEND_2
            3 -> RECOMMEND_3
            4 -> RECOMMEND_4
            5 -> RECOMMEND_5
            else -> RECOMMEND_MORE
        }

        return type
    }

    override fun onClick(view: View) {
        var tag = view.tag as? RecyclerView.ViewHolder
        if (tag == null) {
            tag = (view.parent as? View)?.tag as? RecyclerView.ViewHolder
        }

        val position = tag?.adapterPosition ?: -1

        when (view.id) {
            R.id.rlClickableRoot -> onRecommendedAccountClick(view, view.tag as ShortAccountModel)
            R.id.btnReply -> if (position >= 0) onReplyClick(getDataItemByAdapterPosition(position))
            R.id.commentRoot -> if (position >= 0) onCommentOptionsClick(view, getDataItemByAdapterPosition(position))
            R.id.btnUseful -> if (position >= 0) onCommentUsefulClick(getDataItemByAdapterPosition(position))
            R.id.ivAvatar -> if (position >= 0) onCommentAuthorClick(getDataItemByAdapterPosition(position).creator)
            R.id.tvUserName -> if (position >= 0) onCommentAuthorClick(getDataItemByAdapterPosition(position).creator)
            R.id.ivOne -> if (position >= 0) onImageClickListener(getDataItemByAdapterPosition(position), 0)
            R.id.ivTwo -> if (position >= 0) onImageClickListener(getDataItemByAdapterPosition(position), 1)
            R.id.ivThree -> if (position >= 0) onImageClickListener(getDataItemByAdapterPosition(position), 2)
        }
    }

    companion object {
        private const val TYPE_COMMENT = 1 shl 1
        private const val TYPE_COMMENT_REPLY = 1 shl 2

        private const val IMAGE_1 = 1 shl 7
        private const val IMAGE_2 = 1 shl 8
        private const val IMAGE_3 = 1 shl 9
        private const val IMAGE_MORE = 1 shl 10

        private const val RECOMMEND_1 = 1 shl 11
        private const val RECOMMEND_2 = 1 shl 12
        private const val RECOMMEND_3 = 1 shl 13
        private const val RECOMMEND_4 = 1 shl 14
        private const val RECOMMEND_5 = 1 shl 15
        private const val RECOMMEND_MORE = 1 shl 16

        private fun Int.hasFlag(flag: Int) = this and flag == flag

        private fun Int.getImagesCount() = when {
            hasFlag(IMAGE_1) -> 1
            hasFlag(IMAGE_2) -> 2
            hasFlag(IMAGE_3) -> 3
            hasFlag(IMAGE_MORE) -> 4
            else -> 0
        }
    }

    private class CommentViewHolder(itemView: View,
                                    private val onClickListener: View.OnClickListener,
                                    private val commentsRewardModel: CommentsRewardModel) : BaseVH<CommentModel>(itemView), View.OnLongClickListener {

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

            bindImages(item)

            avatar.setOnClickListener(onClickListener)
            (avatar.parent as? View)?.tag = this

            userName.setOnClickListener(onClickListener)
            userName.tag = this
        }

        private fun handleUseful(item: CommentModel, usefulButton: Button, isMyPost: Boolean) {
            when {
                item.creator == ShortAccountModel.EMPTY -> {
                    usefulButton.visibility = View.INVISIBLE
                    usefulButton.isEnabled = false
                }
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

        private fun bindImages(item: CommentModel) {
            val attachments = item.images

            with(itemView) {
                if (attachments.isNotEmpty()) {
                    itemView.findViewById<ImageView>(R.id.ivOne).let {
                        it.image(attachments[0])
                        (it.parent as View).tag = this@CommentViewHolder
                        it.setOnClickListener(onClickListener)
                    }
                }

                if (attachments.size >= 2) {
                    itemView.findViewById<ImageView>(R.id.ivTwo).let {
                        it.image(attachments[1])
                        (it.parent as View).tag = this@CommentViewHolder
                        it.setOnClickListener(onClickListener)
                    }
                }

                if (attachments.size >= 3) {
                    itemView.findViewById<ImageView>(R.id.ivThree).let {
                        it.image(attachments[2])
                        (it.parent as View).tag = this@CommentViewHolder
                        it.setOnClickListener(onClickListener)
                    }
                }

                if (attachments.size > 3) {
                    itemView.findViewById<TextView>(R.id.tvCountMore).text = "+${attachments.size - 2}"
                }
            }
        }

        override fun onLongClick(view: View): Boolean {
            onClickListener.onClick(view)
            return true
        }

        companion object {
            private const val FIRST_ITEM_POSITION = 1

            fun newInstanceComment(
                    parent: ViewGroup,
                    onClickListener: View.OnClickListener,
                    commentRewardModel: CommentsRewardModel,
                    imagesCount: Int
            ): CommentViewHolder {
                val inflater = LayoutInflater.from(parent.context)

                val view = inflater.inflate(R.layout.item_comment, parent, false)
                val flImagesRoot = view.findViewById<ViewGroup>(R.id.flImagesRoot)


                if (imagesCount > 0) {
                    val imagesLayout = when (imagesCount) {
                        1 -> R.layout.item_image_one
                        2 -> R.layout.item_image_two
                        3 -> R.layout.item_image_three
                        else -> R.layout.item_image_more
                    }
                    inflater.inflate(imagesLayout, flImagesRoot, true)
                }

                flImagesRoot.isGone = imagesCount == 0

                return CommentViewHolder(view, onClickListener, commentRewardModel)
            }

            fun newInstanceReply(
                    parent: ViewGroup,
                    onClickListener: View.OnClickListener,
                    commentRewardModel: CommentsRewardModel,
                    imagesCount: Int
            ): CommentViewHolder {
                val inflater = LayoutInflater.from(parent.context)

                val view = inflater.inflate(R.layout.item_comment_reply, parent, false)
                val flImagesRoot = view.findViewById<ViewGroup>(R.id.flImagesRoot)

                if (imagesCount > 0) {
                    val imagesLayout = when (imagesCount) {
                        1 -> R.layout.item_image_one
                        2 -> R.layout.item_image_two
                        3 -> R.layout.item_image_three
                        else -> R.layout.item_image_more
                    }
                    inflater.inflate(imagesLayout, flImagesRoot, true)
                }

                flImagesRoot.isGone = imagesCount == 0

                return CommentViewHolder(view, onClickListener, commentRewardModel)
            }
        }
    }

    private class HeaderViewHolder(itemView: View) : BaseVH<Any>(itemView) {
        override fun bind(item: Any) = Unit
    }
}
