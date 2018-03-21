package com.mnassa.screen.connections.allconnections

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/14/2018.
 */
class AllConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), AllConnectionsViewModel {

    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getConnectedConnections().consumeEach {
                allConnectionsChannel.send(it)
            }
        }
    }

    override fun disconnect(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionDisconnect(listOf(account.id))
            }
        }
    }

}