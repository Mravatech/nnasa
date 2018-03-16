package com.mnassa.screen.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    var onItemViewedListsner: (item: Post) -> Unit = { }

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
            onItemViewedListsner(dataStorage[position])
        }
    }

    override fun getViewType(position: Int): Int = dataStorage[position].type.ordinal

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}