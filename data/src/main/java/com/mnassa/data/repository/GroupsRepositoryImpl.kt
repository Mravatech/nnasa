package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseGroupsApi
import com.mnassa.data.network.bean.firebase.GroupDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.CreateGroupRequest
import com.mnassa.data.network.bean.retrofit.request.GroupConnectionRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.GroupModelImpl
import com.mnassa.domain.model.impl.ShortAccountModelImpl
import com.mnassa.domain.repository.GroupsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 5/21/2018.
 */
class GroupsRepositoryImpl(
        private val firestore: FirebaseFirestore,
        private val userRepository: UserRepository,
        private val exceptionHandler: ExceptionHandler,
        private val converter: ConvertersContext,
        private val db: DatabaseReference,
        private val api: FirebaseGroupsApi
) : GroupsRepository {

    override suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS)
                .document(userRepository.getAccountIdOrException())
                .collection(DatabaseContract.TABLE_GROUPS_COL_MY)
                .toListChannel<GroupDbEntity>(exceptionHandler)
                .map { it.map { convertGroup(it, forceMyGroup = true) } }
    }

    override suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS)
                .document(userRepository.getAccountIdOrException())
                .collection(DatabaseContract.TABLE_GROUPS_COL_INVITES)
                .toListChannel<GroupDbEntity>(exceptionHandler)
                .map { it.map { convertGroup(it) } }
    }

    override suspend fun sendInvite(groupId: String, accountIds: List<String>) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.INVITE,
                groupId = groupId,
                accounts = accountIds
        )).handleException(exceptionHandler)
    }

    override suspend fun acceptInvite(groupId: String) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.ACCEPT_INVITE,
                groupId = groupId
        )).handleException(exceptionHandler)
    }

    override suspend fun declineInvite(groupId: String) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.DECLINE_INVITE,
                groupId = groupId
        )).handleException(exceptionHandler)
    }

    override suspend fun leaveGroup(groupId: String) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.LEAVE,
                groupId = groupId
        )).handleException(exceptionHandler)
    }

    override suspend fun removeFromGroup(groupId: String, accountIds: List<String>) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.REMOVE,
                groupId = groupId,
                accounts = accountIds
        )).handleException(exceptionHandler)
    }

    override suspend fun makeAdmin(groupId: String, accountIds: List<String>) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.MAKE_ADMIN,
                groupId = groupId,
                accounts = accountIds
        )).handleException(exceptionHandler)
    }

    override suspend fun unMakeAdmin(groupId: String, accountIds: List<String>) {
        api.inviteAction(GroupConnectionRequest(
                action = NetworkContract.GroupInviteAction.UN_MAKE_ADMIN,
                groupId = groupId,
                accounts = accountIds
        )).handleException(exceptionHandler)
    }

    override suspend fun getGroup(groupId: String): ReceiveChannel<GroupModel?> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                .document(groupId)
                .toValueChannel<GroupDbEntity>(exceptionHandler)
                .map { it?.run { convertGroup(it) } }
    }

    override suspend fun getGroupMembers(groupId: String): ReceiveChannel<List<ShortAccountModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                .document(groupId)
                .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_MEMBERS)
                .toListChannel<ShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, ShortAccountModel::class.java) }
    }

    override suspend fun createGroup(group: RawGroupModel): GroupModel {
        return api.create(makeRequest(group)).handleException(exceptionHandler).group.let { convertGroup(it) }
    }

    override suspend fun updateGroup(group: RawGroupModel) {
        api.update(makeRequest(group)).handleException(exceptionHandler)
    }

    private suspend fun convertGroup(input: GroupDbEntity, forceMyGroup: Boolean = false): GroupModel {
        val currentUserId = userRepository.getAccountIdOrException()

        val creator = when {
            input.author != null -> converter.convert(input.author, ShortAccountModel::class.java)
            input.isAdmin == true || forceMyGroup -> userRepository.getCurrentAccountOrException()
            input.admins?.isNotEmpty() == true -> userRepository.getProfileByAccountId(input.admins.first())
            else -> ShortAccountModelImpl.EMPTY
        }

        return GroupModelImpl(
                id = input.id,
                name = input.title ?: "Unnamed group",
                description = input.description ?: "",
                avatar = input.avatar,
                type = GroupType.Private(),
                admins = input.admins
                        ?: (if (input.isAdmin == true) listOf(currentUserId) else emptyList()),
                numberOfParticipants = input.counters?.numberOfParticipants ?: 0L,
                creator = creator,
                website = input.website,
                locationPlace = input.location?.let { converter.convert(it, LocationPlaceModel::class.java) },
                tags = input.tags ?: emptyList())
    }

    private fun makeRequest(group: RawGroupModel): CreateGroupRequest {
        return CreateGroupRequest(
                communityId = group.id,
                id = group.id,
                description = group.description,
                title = group.title,
                website = group.website,
                location = group.placeId,
                avatar = group.avatarUploaded,
                tags = group.processedTags.takeIf { it.isNotEmpty() }
        )
    }
}