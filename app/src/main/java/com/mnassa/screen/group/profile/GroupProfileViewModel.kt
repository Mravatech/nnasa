package com.mnassa.screen.group.profile

import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupProfileViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>
    val tagsChannel: BroadcastChannel<List<TagModel>>
    val closeScreenChannel: BroadcastChannel<Unit>

    val newsFeedChannel: BroadcastChannel<ListItemEvent<PostModel>>
    val infoFeedChannel: BroadcastChannel<ListItemEvent<InfoPostModel>>
    val permissionsChannel: BroadcastChannel<PermissionsModel>
    fun onAttachedToWindow(post: PostModel)
    fun hideInfoPost(post: PostModel)
    fun leave()
}