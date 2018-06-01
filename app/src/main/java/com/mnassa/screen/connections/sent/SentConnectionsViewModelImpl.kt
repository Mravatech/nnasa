package com.mnassa.screen.connections.sent

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class SentConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), SentConnectionsViewModel {

    override val sentConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getSentConnections().consumeEach {
                sentConnectionsChannel.send(it)
            }
        }
    }

    override fun cancelRequest(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionRevoke(listOf(account.id))
            }
        }
    }
}