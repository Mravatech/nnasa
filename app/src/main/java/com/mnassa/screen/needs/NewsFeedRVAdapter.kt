package com.mnassa.screen.needs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.domain.model.NewsFeedItem
import com.mnassa.domain.model.NewsFeedItemType
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.needs.viewholder.GeneralViewHolder
import com.mnassa.screen.needs.viewholder.NeedViewHolder
import com.mnassa.screen.needs.viewholder.OfferViewHolder
import com.mnassa.screen.needs.viewholder.ProfileViewHolder

/**
 * Created by Peter on 3/14/2018.
 */
class NewsFeedRVAdapter : BasePaginationRVAdapter<NewsFeedItem>(), View.OnClickListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<NewsFeedItem> {
        return when (viewType) {
            NewsFeedItemType.GENERAL.ordinal -> GeneralViewHolder.newInstance(parent, this)
            NewsFeedItemType.NEED.ordinal -> NeedViewHolder.newInstance(parent, this)
            NewsFeedItemType.OFFER.ordinal -> OfferViewHolder.newInstance(parent, this)
            NewsFeedItemType.PROFILE.ordinal -> ProfileViewHolder.newInstance(parent, this)
            else -> throw IllegalStateException("Illegal view type $viewType")
        }
    }

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}