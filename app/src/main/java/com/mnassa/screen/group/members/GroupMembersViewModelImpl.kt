package com.mnassa.screen.group.members

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
class GroupMembersViewModelImpl(
        private val groupId: String,
        private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupMembersViewModel {

    override val groupMembersChannel: BroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getGroupMembers(groupId).consumeTo(groupMembersChannel)
        }
    }
}