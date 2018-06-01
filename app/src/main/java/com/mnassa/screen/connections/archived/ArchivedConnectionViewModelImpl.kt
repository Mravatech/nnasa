package com.mnassa.screen.connections.archived

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class ArchivedConnectionViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ArchivedConnectionViewModel {

    override val declinedConnectionsChannel: ConflatedBroadcastChannel<List<DeclinedShortAccountModel>> = ConflatedBroadcastChannel()

    override suspend fun getDisconnectTimeoutDays(): Int = handleExceptionsSuspend { connectionsInteractor.getDisconnectTimeoutDays() } ?: 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getDisconnectTimeoutDays()

            connectionsInteractor.getDisconnectedConnections().consumeEach {
                declinedConnectionsChannel.send(it)
            }
        }
    }

    override fun connect(account: DeclinedShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(account.id))
            }
        }
    }
}