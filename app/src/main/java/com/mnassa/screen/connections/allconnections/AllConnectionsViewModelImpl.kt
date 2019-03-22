package com.mnassa.screen.connections.allconnections

import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/14/2018.
 */
class AllConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), AllConnectionsViewModel {

    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getConnectedConnections().consumeTo(allConnectionsChannel)
        }
    }

    override fun disconnect(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionDisconnect(listOf(account.id))
            }
        }
    }
}