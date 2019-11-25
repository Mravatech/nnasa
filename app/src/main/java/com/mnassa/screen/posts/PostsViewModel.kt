package com.mnassa.screen.posts

import com.mnassa.domain.aggregator.AggregatorLive
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.widget.newpanel.NewPanelViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface PostsViewModel : MnassaViewModel, NewPanelViewModel {
    val postsLive: AggregatorLive<PostModel>

    val permissionsChannel: BroadcastChannel<PermissionsModel>
    fun onAttachedToWindow(post: PostModel)
    fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int)
    fun hideInfoPost(post: PostModel)

    fun saveScrollPosition(post: PostModel)
    fun restoreScrollPosition(): String?
    fun resetScrollPosition()

    fun repost(sharingOptions: PostPrivacyOptions)

}