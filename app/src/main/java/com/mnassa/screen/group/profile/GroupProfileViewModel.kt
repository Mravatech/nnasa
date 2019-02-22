package com.mnassa.screen.group.profile

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.GroupPermissions
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupProfileViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>
    val tagsChannel: BroadcastChannel<List<TagModel>>
    val closeScreenChannel: BroadcastChannel<Unit>
    val groupPermissionsChannel: BroadcastChannel<GroupPermissions>
    val isMemberChannel: BroadcastChannel<Boolean>

    fun leave()
    fun delete()
}