package com.mnassa.screen.connections.sent

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class SentConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), SentConnectionsViewModel {

    override val sentConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getSentConnections().consumeEach {
                sentConnectionsChannel.send(it)
            }
        }
    }

    override fun cancelRequest(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionRevoke(listOf(account.id))
            }
        }
    }
}