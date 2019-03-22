package com.mnassa.screen.group.invite

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 5/23/2018.
 */
class GroupInviteConnectionsViewModelImpl(
        val groupId: String,
        val groupsInteractor: GroupsInteractor,
        val connectionsInteractor: ConnectionsInteractor
) : MnassaViewModelImpl(), GroupInviteConnectionsViewModel {

    override val connectionsChannel: BroadcastChannel<List<UserInvite>> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = ConflatedBroadcastChannel()

    private val userConnections = ConflatedBroadcastChannel<List<ShortAccountModel>>()
    private val groupMembers = ConflatedBroadcastChannel<List<ShortAccountModel>>()
    private val invitedUsers = ConflatedBroadcastChannel<Set<ShortAccountModel>>()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getConnectedConnections().consumeEach {
                userConnections.send(it)
                refreshData()
            }
        }
        setupScope.launchWorker {
            groupsInteractor.getGroupMembers(groupId).consumeEach {
                groupMembers.send(it)
                refreshData()
            }
        }
        setupScope.launchWorker {
            groupsInteractor.getInvitedUsers(groupId).consumeEach {
                invitedUsers.send(it)
                refreshData()
            }
        }
    }

    private suspend fun refreshData() {
        val allUserConnections = userConnections.consume{ receive() }
        val groupMembers = groupMembers.consume { receive() }
        val invitedUsers = invitedUsers.consume { receive() }
        allUserConnections.map { user ->
            UserInvite(
                    user = user,
                    isInvited = invitedUsers.any { it.id == user.id },
                    isMember = groupMembers.any { it.id == user.id }
            )
        }.let { connectionsChannel.send(it) }
    }

    override fun sendInvite(user: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.sendInvite(groupId, listOf(user.id))
            }
        }
    }

    override fun revokeInvite(user: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.revokeInvite(groupId, listOf(user.id))
            }
        }
    }

    override fun removeUser(user: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.removeMember(groupId, user.id)
            }
        }
    }
}