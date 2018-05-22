package com.mnassa.domain.repository

import com.mnassa.domain.model.GroupModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 5/21/2018.
 */
interface GroupsRepository {
    suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>>
    suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>>
    //
    suspend fun sendInvite(groupId: String, accountIds: List<String>)
    suspend fun acceptInvite(groupId: String)
    suspend fun declineInvite(groupId: String)
    suspend fun leaveGroup(groupId: String)
    //
    suspend fun getGroup(groupId: String): ReceiveChannel<GroupModel?>
}