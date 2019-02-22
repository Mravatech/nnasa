package com.mnassa.screen.connections.newrequests

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class NewRequestsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), NewRequestsViewModel {
    override val newConnectionRequestsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            connectionsInteractor.getConnectionRequests().consumeEach {
                newConnectionRequestsChannel.send(it)
            }
        }
    }

    override suspend fun getDisconnectTimeoutDays(): Int = handleExceptionsSuspend { connectionsInteractor.getDisconnectTimeoutDays() } ?: 0

    override fun accept(account: ShortAccountModel) {
        resolveExceptions {
            withProgressSuspend {
                connectionsInteractor.actionAccept(listOf(account.id))
            }
        }
    }

    override fun decline(account: ShortAccountModel) {
        resolveExceptions {
            withProgressSuspend {
                connectionsInteractor.actionDecline(listOf(account.id))
            }
        }
    }
}