package com.mnassa.screen.connections.select

import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
class SelectConnectionViewModelImpl(private val additionalData: SelectConnectionViewModel.AdditionalData,
                                    private val connectionsInteractor: ConnectionsInteractor,
                                    private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), SelectConnectionViewModel {

    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)

        val data = additionalData
        if (data is SelectConnectionViewModel.AdditionalData.IncludeGroupMembers) {
            setupScope.loadConnectionsWithGroupMembers(data.groupId)
        } else {
            setupScope.loadConnections()
        }
    }

    private fun CoroutineScope.loadConnections() {
        launchWorker {
            connectionsInteractor.getConnectedConnections().consumeTo(allConnectionsChannel)
        }
    }

    private fun CoroutineScope.loadConnectionsWithGroupMembers(groupId: String) {
        launchWorker {
            val connections = connectionsInteractor.getConnectedConnections().receive()
            val groupMembers = groupsInteractor.getGroupMembers(groupId).receive()
            val result = (connections + groupMembers).distinctBy { it.id }
            allConnectionsChannel.send(result)
        }
    }
}