package com.mnassa.screen.group.requests

import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 5/22/2018.
 */
interface GroupConnectionRequestsViewModel : MnassaViewModel {
    val groupConnectionRequestsChannel: BroadcastChannel<List<GroupModel>>

    fun accept(group: GroupModel)
    fun decline(group: GroupModel)
}