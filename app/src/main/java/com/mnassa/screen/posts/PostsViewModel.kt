package com.mnassa.screen.posts

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface PostsViewModel : MnassaViewModel {
    val newsFeedChannel: BroadcastChannel<ListItemEvent<PostModel>>
    val permissionsChannel: BroadcastChannel<PermissionsModel>
    fun onAttachedToWindow(post: PostModel)
}