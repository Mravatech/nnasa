package com.mnassa.screen.posts

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.ReConsumeWhenAccountChangedArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class PostsViewModelImpl(private val postsInteractor: PostsInteractor) : MnassaViewModelImpl(), PostsViewModel {

    override val newsFeedChannel: BroadcastChannel<ListItemEvent<PostModel>> by ReConsumeWhenAccountChangedArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { postsInteractor.loadAll() })

    override fun onAttachedToWindow(post: PostModel) {
        handleException { postsInteractor.onItemViewed(post) }
    }
}