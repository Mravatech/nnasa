package com.mnassa.screen.connections

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ConnectionsViewModel {
    override val requestedConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val recommendedConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            connectionsInteractor.getRequestedConnections().consumeEach {
                requestedConnectionsChannel.send(it)
            }
        }

        launchCoroutineUI {
            connectionsInteractor.getRecommendedConnections().consumeEach {
                recommendedConnectionsChannel.send(it)
            }
        }

        launchCoroutineUI {
            connectionsInteractor.getConnectedConnections().consumeEach {
                allConnectionsChannel.send(it)
            }
        }
    }
}