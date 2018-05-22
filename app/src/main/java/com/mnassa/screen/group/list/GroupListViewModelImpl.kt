package com.mnassa.screen.group.list

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/14/2018.
 */
class GroupListViewModelImpl(
        private val groupsInteractor: GroupsInteractor,
        private val userProfileInteractor: UserProfileInteractor

) : MnassaViewModelImpl(), GroupListViewModel {

    override val groupConnectionRequestsChannel: BroadcastChannel<List<GroupModel>> by ProcessAccountChangeConflatedBroadcastChannel {
        groupsInteractor.getInvitesToGroups()
    }

    override val myGroupsChannel: BroadcastChannel<List<GroupModel>> by ProcessAccountChangeConflatedBroadcastChannel {
        groupsInteractor.getMyGroups()
    }

    override val permissionsChannel: BroadcastChannel<PermissionsModel> by ProcessAccountChangeConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override fun leave(group: GroupModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.leaveGroup(groupId = group.id)
            }
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