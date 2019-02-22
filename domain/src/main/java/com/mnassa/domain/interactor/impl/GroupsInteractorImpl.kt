package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.GroupsRepository
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 5/21/2018.
 */
class GroupsInteractorImpl(private val groupsRepository: GroupsRepository,
                           private val storageInteractor: StorageInteractor,
                           private val tagInteractor: TagInteractor,
                           private val userInteractor: UserProfileInteractor) : GroupsInteractor {

    override suspend fun deleteGroup(groupId: String) = groupsRepository.deleteGroup(groupId)

    override suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>> = groupsRepository.getMyGroups()

    override suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>> = groupsRepository.getInvitesToGroups()

    override suspend fun getHasInviteToGroupChannel(groupId: String): ReceiveChannel<Boolean> = groupsRepository.getHasInviteToGroupChannel(groupId)

    override suspend fun sendInvite(groupId: String, accountIds: List<String>) = groupsRepository.sendInvite(groupId, accountIds)

    override suspend fun revokeInvite(groupId: String, accountIds: List<String>) = groupsRepository.revokeInvite(groupId, accountIds)

    override suspend fun acceptInvite(groupId: String) = groupsRepository.acceptInvite(groupId)

    override suspend fun declineInvite(groupId: String) = groupsRepository.declineInvite(groupId)

    override suspend fun leaveGroup(groupId: String) = groupsRepository.leaveGroup(groupId)

    override suspend fun removeMember(groupId: String, memberId: String) = groupsRepository.removeFromGroup(groupId, listOf(memberId))

    override suspend fun makeAdmin(groupId: String, memberId: String) = groupsRepository.makeAdmin(groupId, listOf(memberId))

    override suspend fun unMakeAdmin(groupId: String, memberId: String) = groupsRepository.unMakeAdmin(groupId, listOf(memberId))

    override suspend fun getGroupById(groupId: String): GroupModel? = groupsRepository.getGroupById(groupId)

    override suspend fun getGroup(groupId: String): ReceiveChannel<GroupModel?> = groupsRepository.getGroup(groupId)

    override suspend fun getGroupMembers(groupId: String): ReceiveChannel<List<ShortAccountModel>> = groupsRepository.getGroupMembers(groupId)

    override suspend fun createGroup(group: RawGroupModel): GroupModel = groupsRepository.createGroup(group.copy(
            avatarUploaded = uploadAvatar(group),
            processedTags = processTags(group)))

    override suspend fun updateGroup(group: RawGroupModel) = groupsRepository.updateGroup(group.copy(
            avatarUploaded = uploadAvatar(group),
            processedTags = processTags(group)))

    override suspend fun getInvitedUsers(groupId: String): ReceiveChannel<Set<ShortAccountModel>> = groupsRepository.getInvitedUsers(groupId)
    override suspend fun hasAnyGroup(): Boolean = groupsRepository.hasAnyGroup()

    private suspend fun uploadAvatar(group: RawGroupModel): String? {
        if (group.avatarToUpload == null) {
            return group.avatarUploaded
        }

        return storageInteractor.sendImage(StoragePhotoDataImpl(group.avatarToUpload, FOLDER_GROUPS))
    }

    private suspend fun processTags(post: RawGroupModel): List<String> {
        val customTagsAndTagsWithIds: List<TagModel> = post.tags

        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name.toString() }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags)
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }
}