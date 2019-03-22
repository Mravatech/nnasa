package com.mnassa.screen.connections.newrequests

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
class NewRequestsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), NewRequestsViewModel {
    override val newConnectionRequestsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getConnectionRequests().consumeEach {
                newConnectionRequestsChannel.send(it)
            }
        }
    }

    override suspend fun getDisconnectTimeoutDays(): Int = handleExceptionsSuspend { connectionsInteractor.getDisconnectTimeoutDays() } ?: 0

    override fun accept(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionAccept(listOf(account.id))
            }
        }
    }

    override fun decline(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionDecline(listOf(account.id))
            }
        }
    }
}