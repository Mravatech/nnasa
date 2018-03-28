package com.mnassa.screen.posts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostType
import com.mnassa.extensions.isRepost
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.posts.viewholder.*

/**
 * Created by Peter on 3/14/2018.
 */
class PostsRVAdapter : BaseSortedPaginationRVAdapter<PostModel>(), View.OnClickListener {
    var onAttachedToWindow: (item: PostModel) -> Unit = { }
    var onDetachedFromWindow: (item: PostModel) -> Unit = { }
    var onItemClickListener = { item: PostModel -> }
    var onCreateNeedClickListener = {}

    override val itemsComparator: (item1: PostModel, item2: PostModel) -> Int = { first, second ->
        first.createdAt.compareTo(second.createdAt) * -1
    }
    override val itemClass: Class<PostModel> = PostModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id}
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = SortedDataStorage(itemClass, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER) HeaderViewHolder.newInstance(parent, this) else
            super.onCreateViewHolder(parent, viewType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<PostModel> {
        return when (viewType) {
            TYPE_GENERAL -> GeneralViewHolder.newInstance(parent, this)
            TYPE_NEED -> NeedViewHolder.newInstance(parent, this)
            TYPE_NEED_WITH_IMAGE_1 -> NeedViewHolder.newInstance(parent, this, imagesCount = 1)
            TYPE_NEED_WITH_IMAGE_2 -> NeedViewHolder.newInstance(parent, this, imagesCount = 2)
            TYPE_NEED_WITH_IMAGE_3 -> NeedViewHolder.newInstance(parent, this, imagesCount = 3)
            TYPE_NEED_WITH_IMAGE_MORE -> NeedViewHolder.newInstance(parent, this, imagesCount = 4)

            TYPE_NEED_REPOST ->  NeedViewHolder.newInstance(parent, this, isRepost = true)
            TYPE_NEED_WITH_IMAGE_1_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 1, isRepost = true)
            TYPE_NEED_WITH_IMAGE_2_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 2, isRepost = true)
            TYPE_NEED_WITH_IMAGE_3_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 3, isRepost = true)
            TYPE_NEED_WITH_IMAGE_MORE_REPOST -> NeedViewHolder.newInstance(parent, this, imagesCount = 4, isRepost = true)

            TYPE_OFFER -> OfferViewHolder.newInstance(parent, this)
            TYPE_PROFILE -> ProfileViewHolder.newInstance(parent, this)
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
            PostType.NEED -> when (item.images.size) {
                0 -> if (item.isRepost) TYPE_NEED_REPOST else TYPE_NEED
                1 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_1_REPOST else TYPE_NEED_WITH_IMAGE_1
                2 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_2_REPOST else TYPE_NEED_WITH_IMAGE_2
                3 -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_3_REPOST else TYPE_NEED_WITH_IMAGE_3
                else -> if (item.isRepost) TYPE_NEED_WITH_IMAGE_MORE_REPOST else TYPE_NEED_WITH_IMAGE_MORE
            }
            PostType.OFFER -> TYPE_OFFER
            PostType.GENERAL -> TYPE_GENERAL
            PostType.PROFILE -> TYPE_PROFILE
        }
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
            R.id.flCreateNeed -> onCreateNeedClickListener()
        }
    }

    private companion object {
        private const val TYPE_GENERAL = 1
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

        private const val TYPE_OFFER = 12
        private const val TYPE_PROFILE = 13
    }
}