package com.mnassa.screen.connections.newrequests

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class NewRequestsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), NewRequestsViewModel {
    override val newConnectionRequestsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getConnectionRequests().consumeEach {
                newConnectionRequestsChannel.send(it)
            }
        }
    }

    override suspend fun getDisconnectTimeoutDays(): Int {
        var result = 0
        handleExceptionsSuspend {
            result = connectionsInteractor.getDisconnectTimeoutDays()
        }
        return result
    }

    override fun accept(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionAccept(listOf(account.id))
            }
        }
    }

    override fun decline(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionDecline(listOf(account.id))
            }
        }
    }
}