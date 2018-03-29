package com.mnassa.screen.posts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostType
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.posts.viewholder.GeneralViewHolder
import com.mnassa.screen.posts.viewholder.NeedViewHolder
import com.mnassa.screen.posts.viewholder.OfferViewHolder
import com.mnassa.screen.posts.viewholder.ProfileViewHolder

/**
 * Created by Peter on 3/14/2018.
 */
class PostsRVAdapter : BaseSortedPaginationRVAdapter<Post>(), View.OnClickListener {
    var onAttachedToWindow: (item: Post) -> Unit = { }
    var onDetachedFromWindow: (item: Post) -> Unit = { }
    var onItemClickListener = { item: Post -> }

    override val itemsComparator: (item1: Post, item2: Post) -> Int = { first, second ->
        first.createdAt.compareTo(second.createdAt) * -1
    }
    override val itemClass: Class<Post> = Post::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id}
        contentTheSameComparator = { first, second -> first == second }
        dataStorage = SortedDataStorage(itemClass, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<Post> {
        return when (viewType) {
            TYPE_GENERAL -> GeneralViewHolder.newInstance(parent, this)
            TYPE_NEED -> NeedViewHolder.newInstance(parent, this)
            TYPE_NEED_WITH_IMAGE_1 -> NeedViewHolder.newInstanceWithImage(parent, this, imagesCount = 1)
            TYPE_NEED_WITH_IMAGE_2 -> NeedViewHolder.newInstanceWithImage(parent, this, imagesCount = 2)
            TYPE_NEED_WITH_IMAGE_3 -> NeedViewHolder.newInstanceWithImage(parent, this, imagesCount = 3)
            TYPE_NEED_WITH_IMAGE_MORE -> NeedViewHolder.newInstanceWithImage(parent, this, imagesCount = 4)
            TYPE_OFFER -> OfferViewHolder.newInstance(parent, this)
            TYPE_PROFILE -> ProfileViewHolder.newInstance(parent, this)
            else -> throw IllegalStateException("Illegal view type $viewType")
        }
    }

    override fun onViewAttachedToWindow(holder: BaseVH<Post>) {
        super.onViewAttachedToWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition)
        if (position >= 0) {
            onAttachedToWindow(dataStorage[position])
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseVH<Post>) {
        super.onViewDetachedFromWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition)
        if (position >= 0) {
            onDetachedFromWindow(dataStorage[position])
        }
    }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]
        return when (item.type) {
            PostType.NEED -> if (item.images.isEmpty()) TYPE_NEED else when (item.images.size) {
                1 -> TYPE_NEED_WITH_IMAGE_1
                2 -> TYPE_NEED_WITH_IMAGE_2
                3 -> TYPE_NEED_WITH_IMAGE_3
                else -> TYPE_NEED_WITH_IMAGE_MORE
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
            R.id.rlClickableRoot -> {
                onItemClickListener(getDataItemByAdapterPosition(position))
            }
        }
    }

    private companion object {
        private const val TYPE_GENERAL = 1
        private const val TYPE_NEED = 2
        private const val TYPE_NEED_WITH_IMAGE_1 = 3
        private const val TYPE_NEED_WITH_IMAGE_2 = 4
        private const val TYPE_NEED_WITH_IMAGE_3 = 5
        private const val TYPE_NEED_WITH_IMAGE_MORE = 6
        private const val TYPE_OFFER = 7
        private const val TYPE_PROFILE = 8
    }
}