package com.mnassa.screen.posts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.InfoPostModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostType
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.isRepost
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.posts.viewholder.*

/**
 * Created by Peter on 3/14/2018.
 */
open class PostsRVAdapter : BaseSortedPaginationRVAdapter<PostModel>(), View.OnClickListener {
    var onAttachedToWindow: (item: PostModel) -> Unit = { }
    var onDetachedFromWindow: (item: PostModel) -> Unit = { }
    var onItemClickListener = { item: PostModel -> }
    var onCreateNeedClickListener = {}
    var onRepostedByClickListener = { account: ShortAccountModel -> }
    var onPostedByClickListener = { account: ShortAccountModel -> }
    var onHideInfoPostClickListener = { post: PostModel -> }

    fun destroyCallbacks() {
        onAttachedToWindow = {}
        onDetachedFromWindow = {}
        onItemClickListener = {}
        onCreateNeedClickListener = {}
        onRepostedByClickListener = {}
        onPostedByClickListener = {}
        onHideInfoPostClickListener = { }
    }

    override val itemsComparator: (item1: PostModel, item2: PostModel) -> Int = { first, second ->
        val firstPinned = (first as? InfoPostModel)?.isPinned ?: false
        val secondPinned = (second as? InfoPostModel)?.isPinned ?: false
        when {
            firstPinned && !secondPinned -> -1
            secondPinned && !firstPinned -> 1
            else -> first.createdAt.compareTo(second.createdAt) * -1
        }
    }
    override val itemClass: Class<PostModel> = PostModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = SortedDataStorage(itemClass, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER) HeaderViewHolder.newInstance(parent, this) else super.onCreateViewHolder(parent, viewType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<PostModel> {
        return when (viewType) {
            TYPE_NEED -> NeedViewHolder.newInstance(parent, this)
            TYPE_NEED_WITH_IMAGE_1 -> NeedViewHolder.newInstance(parent, this, imagesCount = 1)
            TYPE_NEED_WITH_IMAGE_2 -> NeedViewHolder.newInstance(parent, this, imagesCount = 2)
            TYPE_NEED_WITH_IMAGE_3 -> NeedViewHolder.newInstance(parent, this, imagesCount = 3)
            TYPE_NEED_WITH_IMAGE_MORE -> NeedViewHolder.newInstance(parent, this, imagesCount = 4)
            //
            TYPE_NEED_REPOST -> NeedViewHolder.newInstance(parent, this, isRepost = true)
            TYPE_NEED_WITH_IMAGE_1_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 1, isRepost = true)
            TYPE_NEED_WITH_IMAGE_2_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 2, isRepost = true)
            TYPE_NEED_WITH_IMAGE_3_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 3, isRepost = true)
            TYPE_NEED_WITH_IMAGE_MORE_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 4, isRepost = true)
            //
            TYPE_OFFER -> OfferViewHolder.newInstance(parent, this, imagesCount = 0)
            TYPE_OFFER_WITH_IMAGE_1 -> OfferViewHolder.newInstance(parent, this, imagesCount = 1)
            TYPE_OFFER_WITH_IMAGE_2 -> OfferViewHolder.newInstance(parent, this, imagesCount = 2)
            TYPE_OFFER_WITH_IMAGE_3 -> OfferViewHolder.newInstance(parent, this, imagesCount = 3)
            TYPE_OFFER_WITH_IMAGE_MORE -> OfferViewHolder.newInstance(parent, this, imagesCount = 4)
            //
            TYPE_PROFILE -> ProfileViewHolder.newInstance(parent, this)
            //
            TYPE_INFO_PINNED -> InfoViewHolder.newInstance(parent, this, imagesCount = 0, isPinned = true)
            TYPE_INFO -> InfoViewHolder.newInstance(parent, this, imagesCount = 0, isPinned = false)
            TYPE_INFO_WITH_IMAGE_1_PINNED -> InfoViewHolder.newInstance(parent, this, imagesCount = 1, isPinned = true)
            TYPE_INFO_WITH_IMAGE_1 -> InfoViewHolder.newInstance(parent, this, imagesCount = 1, isPinned = false)
            TYPE_INFO_WITH_IMAGE_2_PINNED -> InfoViewHolder.newInstance(parent, this, imagesCount = 2, isPinned = true)
            TYPE_INFO_WITH_IMAGE_2 -> InfoViewHolder.newInstance(parent, this, imagesCount = 2, isPinned = false)
            TYPE_INFO_WITH_IMAGE_3_PINNED -> InfoViewHolder.newInstance(parent, this, imagesCount = 3, isPinned = true)
            TYPE_INFO_WITH_IMAGE_3 -> InfoViewHolder.newInstance(parent, this, imagesCount = 3, isPinned = false)
            TYPE_INFO_WITH_IMAGE_MORE_PINNED -> InfoViewHolder.newInstance(parent, this, imagesCount = 4, isPinned = true)
            TYPE_INFO_WITH_IMAGE_MORE -> InfoViewHolder.newInstance(parent, this, imagesCount = 4, isPinned = false)
            //
            TYPE_OTHER -> UnsupportedTypeViewHolder.newInstance(parent, this)
            else -> throw IllegalStateException("Illegal view type $viewType")
        }
    }

    override fun onViewAttachedToWindow(holder: BaseVH<PostModel>) {
        super.onViewAttachedToWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition)
        if (position >= 0) {
            onAttachedToWindow(dataStorage[position])
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseVH<PostModel>) {
        super.onViewDetachedFromWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition)
        if (position >= 0) {
            onDetachedFromWindow(dataStorage[position])
        }
    }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        return when (item.type) {
            PostType.NEED -> when (item.attachments.size) {
                0 -> if (item.isRepost) TYPE_NEED_REPOST else TYPE_NEED
                1 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_1_REPOST else TYPE_NEED_WITH_IMAGE_1
                2 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_2_REPOST else TYPE_NEED_WITH_IMAGE_2
                3 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_3_REPOST else TYPE_NEED_WITH_IMAGE_3
                else -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_MORE_REPOST else TYPE_NEED_WITH_IMAGE_MORE
            }
            PostType.OFFER -> when (item.attachments.size) {
                0 -> TYPE_OFFER
                1 -> TYPE_OFFER_WITH_IMAGE_1
                2 -> TYPE_OFFER_WITH_IMAGE_2
                3 -> TYPE_OFFER_WITH_IMAGE_3
                else -> TYPE_OFFER_WITH_IMAGE_MORE
            }
            PostType.GENERAL -> when (item.attachments.size) {
                0 -> if (item.isRepost) TYPE_NEED_REPOST else TYPE_NEED
                1 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_1_REPOST else TYPE_NEED_WITH_IMAGE_1
                2 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_2_REPOST else TYPE_NEED_WITH_IMAGE_2
                3 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_3_REPOST else TYPE_NEED_WITH_IMAGE_3
                else -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_MORE_REPOST else TYPE_NEED_WITH_IMAGE_MORE
            }
            PostType.PROFILE -> TYPE_PROFILE
            PostType.INFO -> {
                item as InfoPostModel
                when (item.attachments.size) {
                    0 -> if (item.isPinned) TYPE_INFO_PINNED else TYPE_INFO
                    1 -> if (item.isPinned) TYPE_INFO_WITH_IMAGE_1_PINNED else TYPE_INFO_WITH_IMAGE_1
                    2 -> if (item.isPinned) TYPE_INFO_WITH_IMAGE_2_PINNED else TYPE_INFO_WITH_IMAGE_2
                    3 -> if (item.isPinned) TYPE_INFO_WITH_IMAGE_3_PINNED else TYPE_INFO_WITH_IMAGE_3
                    else -> if (item.isPinned) TYPE_INFO_WITH_IMAGE_MORE_PINNED else TYPE_INFO_WITH_IMAGE_MORE
                }
            }
            PostType.OTHER -> TYPE_OTHER
        }
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
            R.id.flCreateNeed -> onCreateNeedClickListener()
            R.id.rlRepostRoot -> onRepostedByClickListener(requireNotNull(getDataItemByAdapterPosition(position).repostAuthor))
            R.id.rlAuthorRoot -> onPostedByClickListener(getDataItemByAdapterPosition(position).author)
            R.id.btnHidePost -> onHideInfoPostClickListener(getDataItemByAdapterPosition(position))
        }
    }


    private companion object {
        private const val TYPE_NEED = 2
        private const val TYPE_NEED_WITH_IMAGE_1 = 3
        private const val TYPE_NEED_WITH_IMAGE_2 = 4
        private const val TYPE_NEED_WITH_IMAGE_3 = 5
        private const val TYPE_NEED_WITH_IMAGE_MORE = 6

        private const val TYPE_NEED_REPOST = 7
        private const val TYPE_NEED_WITH_IMAGE_1_REPOST = 8
        private const val TYPE_NEED_WITH_IMAGE_2_REPOST = 9
        private const val TYPE_NEED_WITH_IMAGE_3_REPOST = 10
        private const val TYPE_NEED_WITH_IMAGE_MORE_REPOST = 11

        private const val TYPE_PROFILE = 13
        private const val TYPE_PROFILE_REPOST = 14

        private const val TYPE_INFO = 15
        private const val TYPE_INFO_PINNED = 16
        private const val TYPE_INFO_WITH_IMAGE_1 = 17
        private const val TYPE_INFO_WITH_IMAGE_1_PINNED = 18
        private const val TYPE_INFO_WITH_IMAGE_2 = 19
        private const val TYPE_INFO_WITH_IMAGE_2_PINNED = 20
        private const val TYPE_INFO_WITH_IMAGE_3 = 21
        private const val TYPE_INFO_WITH_IMAGE_3_PINNED = 22
        private const val TYPE_INFO_WITH_IMAGE_MORE = 23
        private const val TYPE_INFO_WITH_IMAGE_MORE_PINNED = 24

        private const val TYPE_OFFER = 25
        private const val TYPE_OFFER_WITH_IMAGE_1 = 26
        private const val TYPE_OFFER_WITH_IMAGE_2 = 27
        private const val TYPE_OFFER_WITH_IMAGE_3 = 28
        private const val TYPE_OFFER_WITH_IMAGE_MORE = 29

        private const val TYPE_OTHER = 30
    }
}