package com.mnassa.screen.group.list

import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
class GroupListViewModelImpl(
        private val groupsInteractor: GroupsInteractor,
        private val userProfileInteractor: UserProfileInteractor

) : MnassaViewModelImpl(), GroupListViewModel {

    override val groupConnectionRequestsChannel: BroadcastChannel<List<GroupModel>> = ConflatedBroadcastChannel()
    override val myGroupsChannel: BroadcastChannel<List<GroupModel>> = ConflatedBroadcastChannel()
    override val permissionsChannel: BroadcastChannel<PermissionsModel> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            groupsInteractor.getInvitesToGroups().consumeTo(groupConnectionRequestsChannel)
        }
        setupScope.launchWorker {
            groupsInteractor.getMyGroups().consumeTo(myGroupsChannel)
        }
        setupScope.launchWorker {
            userProfileInteractor.getPermissions().consumeTo(permissionsChannel)
        }
    }

    override fun leave(group: GroupModel) {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.leaveGroup(groupId = group.id)
            }
        }
    }

    override fun accept(group: GroupModel) {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.acceptInvite(groupId = group.id)
            }
        }
    }

    override fun decline(group: GroupModel) {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.declineInvite(groupId = group.id)
            }
        }
    }
}