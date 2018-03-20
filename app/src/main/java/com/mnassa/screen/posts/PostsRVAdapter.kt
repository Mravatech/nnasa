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
            PostType.GENERAL.ordinal -> GeneralViewHolder.newInstance(parent, this)
            PostType.NEED.ordinal -> NeedViewHolder.newInstance(parent, this)
            PostType.OFFER.ordinal -> OfferViewHolder.newInstance(parent, this)
            PostType.PROFILE.ordinal -> ProfileViewHolder.newInstance(parent, this)
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

    override fun getViewType(position: Int): Int = dataStorage[position].type.ordinal

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rlClickableRoot -> {
                onItemClickListener(getDataItemByAdapterPosition(position))
            }
        }
    }
}