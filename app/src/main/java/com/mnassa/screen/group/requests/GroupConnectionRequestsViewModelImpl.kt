package com.mnassa.screen.group.requests

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 5/22/2018.
 */
class GroupConnectionRequestsViewModelImpl(
        private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupConnectionRequestsViewModel {

    override val groupConnectionRequestsChannel: BroadcastChannel<List<GroupModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getInvitesToGroups().consumeTo(groupConnectionRequestsChannel)
        }
    }

    override fun accept(group: GroupModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.acceptInvite(groupId = group.id)
            }
        }
    }

    override fun decline(group: GroupModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.declineInvite(groupId = group.id)
            }
        }
    }
}