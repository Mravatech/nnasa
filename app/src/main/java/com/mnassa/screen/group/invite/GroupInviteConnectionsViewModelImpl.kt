package com.mnassa.screen.group.invite

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 5/23/2018.
 */
class GroupInviteConnectionsViewModelImpl(
        val groupId: String,
        val groupsInteractor: GroupsInteractor,
        val connectionsInteractor: ConnectionsInteractor
) : MnassaViewModelImpl(), GroupInviteConnectionsViewModel {

    override val connectionsChannel: BroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = ConflatedBroadcastChannel()
    private val groupMembers = ConflatedBroadcastChannel<Set<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getConnectedConnections().consumeEach { connections ->
                val members = groupMembers.consume { receive() }
                connectionsChannel.send(connections.filter { !members.contains(it.id) })
            }
        }

        handleException {
            groupsInteractor.getGroupMembers(groupId).consumeEach { members ->
                groupMembers.send(members.map { it.id }.toSet())
            }
        }
    }

    override fun invite(users: List<String>) {
        handleException {
            withProgressSuspend {
                groupsInteractor.sendInvite(groupId, users)
            }
            closeScreenChannel.send(Unit)
        }
    }
}