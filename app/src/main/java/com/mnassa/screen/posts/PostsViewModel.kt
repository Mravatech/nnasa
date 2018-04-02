package com.mnassa.screen.posts

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface PostsViewModel : MnassaViewModel {
    fun <T> handleException(function: suspend () -> T): Job

    suspend fun getNewsFeedChannel(): ReceiveChannel<ListItemEvent<PostModel>>
    fun onAttachedToWindow(post: PostModel)
}