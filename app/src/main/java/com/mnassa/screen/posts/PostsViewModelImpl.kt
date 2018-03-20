package com.mnassa.screen.posts

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/6/2018.
 */
class PostsViewModelImpl(private val postsInteractor: PostsInteractor) : MnassaViewModelImpl(), PostsViewModel {

    override suspend fun getNewsFeedChannel(): ReceiveChannel<ListItemEvent<Post>> = postsInteractor.loadAll()

    override fun onAttachedToWindow(post: Post) {
        handleException { postsInteractor.onItemViewed(post) }
    }

    override fun <T> handleException(function: suspend () -> T): Job {
        return super.handleException(function)
    }

}