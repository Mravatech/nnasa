package com.mnassa.screen.group.list

import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupListViewModel : MnassaViewModel {
    val newConnectionRequestsChannel: BroadcastChannel<List<GroupModel>>
    val recommendedConnectionsChannel: BroadcastChannel<List<GroupModel>>
    val allConnectionsChannel: BroadcastChannel<List<GroupModel>>

    fun connect(group: GroupModel)
    fun disconnect(group: GroupModel)
    fun accept(group: GroupModel)
    fun decline(group: GroupModel)
    suspend fun getDisconnectTimeoutDays(): Int
}