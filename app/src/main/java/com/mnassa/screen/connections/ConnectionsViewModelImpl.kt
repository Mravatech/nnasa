package com.mnassa.screen.connections

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ConnectionsViewModel {

    override val showDeclineConnection: BroadcastChannel<DeclineConnection> = BroadcastChannel(1)

    override val newConnectionRequestsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> by ProcessAccountChangeConflatedBroadcastChannel {
        connectionsInteractor.getConnectionRequests()
    }
    override val recommendedConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> by ProcessAccountChangeConflatedBroadcastChannel {
        connectionsInteractor.getRecommendedConnections()
    }
    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> by ProcessAccountChangeConflatedBroadcastChannel {
        connectionsInteractor.getConnectedConnections()
    }

    override fun connect(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(account.id))
            }
        }
    }

    override fun disconnect(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionDisconnect(listOf(account.id))
            }
        }
    }

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

    override fun declineWithPrompt(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                val timeoutDays = connectionsInteractor.getDisconnectTimeoutDays()
                showDeclineConnection.send(DeclineConnection(account, timeoutDays))
            }
        }
    }

    private var sendPhoneContactsJob: Job? = null
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onContactPermissionsGranted() {
        sendPhoneContactsJob?.cancel()
        sendPhoneContactsJob = launchWorker {
            connectionsInteractor.sendPhoneContacts()
        }
    }
}