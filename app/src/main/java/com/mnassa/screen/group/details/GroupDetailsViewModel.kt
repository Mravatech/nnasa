package com.mnassa.screen.group.details

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupDetailsViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>
    val pointsChannel: BroadcastChannel<Long>
    val isMemberChannel: BroadcastChannel<Boolean>
    val tagsChannel: BroadcastChannel<List<TagModel>>
    val membersChannel: BroadcastChannel<List<ShortAccountModel>>
    val hasInviteChannel: BroadcastChannel<Boolean>
    //
    val closeScreenChannel: BroadcastChannel<Unit>
    val openScreenChannel: BroadcastChannel<ScreenToOpen>

    fun acceptInvite()
    fun declineInvite()

    enum class ScreenToOpen {
        GROUP_PROFILE
    }
}