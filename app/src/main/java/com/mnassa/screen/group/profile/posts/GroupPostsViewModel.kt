package com.mnassa.screen.group.profile.posts

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 09.08.2018.
 */
interface GroupPostsViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>

    fun onAttachedToWindow(post: PostModel)
    fun hideInfoPost(post: PostModel)
    fun removePost(post: PostModel)

    suspend fun getNewsFeedChannel(): ReceiveChannel<ListItemEvent<List<PostModel>>>
}