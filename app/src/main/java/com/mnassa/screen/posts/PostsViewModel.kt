package com.mnassa.screen.posts

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface PostsViewModel : MnassaViewModel {
    suspend fun getNewsFeedChannel(): ReceiveChannel<ListItemEvent<Post>>
    fun onAttachedToWindow(post: Post)
}