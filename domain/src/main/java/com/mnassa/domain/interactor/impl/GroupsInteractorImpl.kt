package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.repository.GroupsRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 5/21/2018.
 */
class GroupsInteractorImpl(private val groupsRepository: GroupsRepository) : GroupsInteractor {

    override suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>> = groupsRepository.getMyGroups()

    override suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>> = groupsRepository.getInvitesToGroups()
}