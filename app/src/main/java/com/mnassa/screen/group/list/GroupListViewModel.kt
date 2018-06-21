package com.mnassa.screen.group.list

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupListViewModel : MnassaViewModel {
    val groupConnectionRequestsChannel: BroadcastChannel<List<GroupModel>>
    val myGroupsChannel: BroadcastChannel<List<GroupModel>>
    val permissionsChannel: BroadcastChannel<PermissionsModel>

    fun leave(group: GroupModel)
    fun accept(group: GroupModel)
    fun decline(group: GroupModel)
}