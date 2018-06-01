package com.mnassa.domain.interactor

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.RawGroupModel
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 5/21/2018.
 */
interface GroupsInteractor {
    suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>>
    suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>>

    suspend fun sendInvite(groupId: String, accountIds: List<String>)
    suspend fun acceptInvite(groupId: String)
    suspend fun declineInvite(groupId: String)
    suspend fun leaveGroup(groupId: String)
    suspend fun removeMember(groupId: String, memberId: String)
    suspend fun makeAdmin(groupId: String, memberId: String)
    suspend fun unMakeAdmin(groupId: String, memberId: String)

    suspend fun getGroup(groupId: String): ReceiveChannel<GroupModel?>
    suspend fun getGroupMembers(groupId: String): ReceiveChannel<List<ShortAccountModel>>

    suspend fun createGroup(group: RawGroupModel): GroupModel
    suspend fun updateGroup(group: RawGroupModel)

    suspend fun getInvitedUsers(groupId: String): ReceiveChannel<Set<ShortAccountModel>>
}