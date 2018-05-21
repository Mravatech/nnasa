package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.network.api.FirebaseGroupsApi
import com.mnassa.data.network.bean.firebase.GroupDbEntity
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.GroupType
import com.mnassa.domain.model.ShortAccountModel
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
                .map { it.map { convertGroup(it) } }
    }

    override suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS)
                .document(userRepository.getAccountIdOrException())
                .collection(DatabaseContract.TABLE_GROUPS_COL_INVITES)
                .toListChannel<GroupDbEntity>(exceptionHandler)
                .map { it.map { convertGroup(it) } }
    }

    //todo: create converter, which supports suspend functions
    private suspend fun convertGroup(input: GroupDbEntity): GroupModel {
        val currentUserId = userRepository.getAccountIdOrException()

        val creator = when {
            input.author != null -> converter.convert(input.author, ShortAccountModel::class.java)
            input.isAdmin == true -> userRepository.getCurrentAccountOrException()
            input.admins?.isNotEmpty() == true -> userRepository.getProfileByAccountId(input.admins.first())
            else -> ShortAccountModelImpl.EMPTY
        }

        return GroupModelImpl(
                id = input.id,
                name = input.title ?: "Unnamed group",
                avatar = input.avatar,
                type = GroupType.Private(),
                admins = input.admins
                        ?: (if (input.isAdmin == true) listOf(currentUserId) else emptyList()),
                isAdmin = input.isAdmin == true,
                numberOfParticipants = input.counters?.numberOfParticipants ?: 0L,
                creator = creator!!)
    }
}