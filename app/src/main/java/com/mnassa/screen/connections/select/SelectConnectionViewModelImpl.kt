package com.mnassa.screen.connections.select

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
class SelectConnectionViewModelImpl(private val additionalData: SelectConnectionViewModel.AdditionalData,
                                    private val connectionsInteractor: ConnectionsInteractor,
                                    private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), SelectConnectionViewModel {

    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = additionalData
        if (data is SelectConnectionViewModel.AdditionalData.IncludeGroupMembers) {
            loadConnectionsWithGroupMembers(data.groupId)
        } else {
            loadConnections()
        }
    }

    private fun loadConnections() {
        handleException {
            connectionsInteractor.getConnectedConnections().consumeTo(allConnectionsChannel)
        }
    }

    private fun loadConnectionsWithGroupMembers(groupId: String) {
        handleException {
            val connections = connectionsInteractor.getConnectedConnections().receive()
            val groupMembers = groupsInteractor.getGroupMembers(groupId).receive()
            val result = (connections + groupMembers).distinctBy { it.id }
            allConnectionsChannel.send(result)
        }
    }
}