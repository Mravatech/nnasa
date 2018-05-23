package com.mnassa.screen.group.members

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupMembersViewModel : MnassaViewModel {
    val groupMembersChannel: BroadcastChannel<List<ShortAccountModel>>
    val groupChannel: BroadcastChannel<GroupModel>

    fun removeMember(member: ShortAccountModel)
    fun makeAdmin(member: ShortAccountModel)
    fun unMakeAdmin(member: ShortAccountModel)
}