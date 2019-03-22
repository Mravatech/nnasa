package com.mnassa.screen.connections.archived

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class ArchivedConnectionViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ArchivedConnectionViewModel {

    override val declinedConnectionsChannel: ConflatedBroadcastChannel<List<DeclinedShortAccountModel>> = ConflatedBroadcastChannel()

    override suspend fun getDisconnectTimeoutDays(): Int = handleExceptionsSuspend { connectionsInteractor.getDisconnectTimeoutDays() } ?: 0

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getDisconnectTimeoutDays()
            connectionsInteractor.getDisconnectedConnections().consumeEach {
                declinedConnectionsChannel.send(it)
            }
        }
    }

    override fun connect(account: DeclinedShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(account.id))
            }
        }
    }
}