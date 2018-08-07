package com.mnassa.screen.group.profile

import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupProfileViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>
    val tagsChannel: BroadcastChannel<List<TagModel>>
    val closeScreenChannel: BroadcastChannel<Unit>
    val groupPermissionsChannel: BroadcastChannel<GroupPermissions>

    suspend fun getNewsFeedChannel(): ReceiveChannel<ListItemEvent<List<PostModel>>>

    fun onAttachedToWindow(post: PostModel)
    fun hideInfoPost(post: PostModel)
    fun leave()
    fun removePost(post: PostModel)
    fun delete()
}