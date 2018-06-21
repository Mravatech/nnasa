package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseGroupsApi
import com.mnassa.data.network.bean.firebase.GroupDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.DeleteGroupRequest
import com.mnassa.data.network.bean.retrofit.request.GroupConnectionRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.RawGroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.GroupsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 5/21/2018.
 */
class GroupsRepositoryImpl(
        private val firestore: FirebaseFirestore,
        userRepositoryLazy: () -> UserRepository,
        private val exceptionHandler: ExceptionHandler,
        converterLazy: () -> ConvertersContext,
        private val api: FirebaseGroupsApi
) : GroupsRepository {

    private val converter by lazy(converterLazy)
    private val userRepository by lazy(userRepositoryLazy)

    override suspend fun deleteGroup(groupId: String) {
        api.delete(DeleteGroupRequest(groupId)).handleException(exceptionHandler)
    }

    override suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS)
                .document(userRepository.getAccountIdOrException())
                .collection(DatabaseContract.TABLE_GROUPS_COL_MY)
                .toListChannel<GroupDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, GroupModel::class.java) }
    }

    override suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS)
                .document(userRepository.getAccountIdOrException())
                .collection(DatabaseContract.TABLE_GROUPS_COL_INVITES)
                .toListChannel<GroupDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, GroupModel::class.java) }
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
                .map { it?.let { converter.convert(it, GroupModel::class.java) } }
    }

    override suspend fun getGroupMembers(groupId: String): ReceiveChannel<List<ShortAccountModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                .document(groupId)
                .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_MEMBERS)
                .toListChannel<ShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, ShortAccountModel::class.java) }
    }

    override suspend fun createGroup(group: RawGroupModel): GroupModel {
        return api.create(converter.convert(group)).handleException(exceptionHandler).group.let { converter.convert(it, GroupModel::class.java) }
    }

    override suspend fun updateGroup(group: RawGroupModel) {
        api.update(converter.convert(group)).handleException(exceptionHandler)
    }

    override suspend fun getInvitedUsers(groupId: String): ReceiveChannel<Set<ShortAccountModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                .document(groupId)
                .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_INVITES)
                .toListChannel<ShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, ShortAccountModel::class.java).toSet() }
    }


}