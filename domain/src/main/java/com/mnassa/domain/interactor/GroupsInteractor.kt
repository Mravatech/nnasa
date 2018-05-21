package com.mnassa.domain.interactor

import com.mnassa.domain.model.GroupModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 5/21/2018.
 */
interface GroupsInteractor {
    suspend fun getMyGroups(): ReceiveChannel<List<GroupModel>>
    suspend fun getInvitesToGroups(): ReceiveChannel<List<GroupModel>>
}