package com.mnassa.screen.connections.recommended

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 09.03.2018.
 */
class RecommendedConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) :
        MnassaViewModelImpl(), RecommendedConnectionsViewModel {

    override val recommendedConnectionsChannel: ConflatedBroadcastChannel<RecommendedConnections> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getRecommendedConnectionsWithGrouping().consumeEach {
                recommendedConnectionsChannel.send(it)
            }
        }
    }

    override fun connect(accountModel: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(accountModel.id))
            }
        }
    }
}