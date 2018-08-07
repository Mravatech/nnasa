package com.mnassa.screen.group.details

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupDetailsViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>
    val isMemberChannel: BroadcastChannel<Boolean>
    val closeScreenChannel: BroadcastChannel<Unit>
    val tagsChannel: BroadcastChannel<List<TagModel>>
    val membersChannel: BroadcastChannel<List<ShortAccountModel>>
    val hasInviteChannel: BroadcastChannel<Boolean>

    fun acceptInvite()
    fun declineInvite()
}