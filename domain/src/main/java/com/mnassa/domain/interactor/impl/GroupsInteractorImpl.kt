package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.model.FOLDER_GROUPS
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.RawGroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.GroupsRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 5/21/2018.
 */
class GroupsInteractorImpl(private val groupsRepository: GroupsRepository,
                           private val storageInteractor: StorageInteractor) : GroupsInteractor {

    override suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>> = groupsRepository.getMyGroups()

    override suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>> = groupsRepository.getInvitesToGroups()

    override suspend fun sendInvite(groupId: String, accountIds: List<String>) = groupsRepository.sendInvite(groupId, accountIds)

    override suspend fun acceptInvite(groupId: String) = groupsRepository.acceptInvite(groupId)

    override suspend fun declineInvite(groupId: String) = groupsRepository.declineInvite(groupId)

    override suspend fun leaveGroup(groupId: String) = groupsRepository.leaveGroup(groupId)

    override suspend fun getGroup(groupId: String): ReceiveChannel<GroupModel?> = groupsRepository.getGroup(groupId)

    override suspend fun getGroupMembers(groupId: String): ReceiveChannel<List<ShortAccountModel>> = groupsRepository.getGroupMembers(groupId)

    override suspend fun createGroup(group: RawGroupModel): GroupModel = groupsRepository.createGroup(group.copy(avatarUploaded = uploadAvatar(group)))

    override suspend fun updateGroup(group: RawGroupModel) = groupsRepository.updateGroup(group.copy(avatarUploaded = uploadAvatar(group)))

    private suspend fun uploadAvatar(group: RawGroupModel): String? {
        if (group.avatarToUpload == null) {
            return group.avatarUploaded
        }

        return storageInteractor.sendImage(StoragePhotoDataImpl(group.avatarToUpload, FOLDER_GROUPS))
    }
}