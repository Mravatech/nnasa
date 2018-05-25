package com.mnassa.screen.group.list

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getInvitesToGroups().consumeTo(groupConnectionRequestsChannel)
        }

        handleException {
            groupsInteractor.getMyGroups().consumeTo(myGroupsChannel)
        }

        handleException {
            userProfileInteractor.getPermissions().consumeTo(permissionsChannel)
        }
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