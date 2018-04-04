package com.mnassa.screen.connections

import android.Manifest
import android.annotation.SuppressLint
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.ReConsumeWhenAccountChangedConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.map
import java.util.*

/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ConnectionsViewModel {
    private val randomSeed = System.currentTimeMillis()
    override val newConnectionRequestsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        connectionsInteractor.getConnectionRequests().map { it.shuffled(Random(randomSeed)) }
    }
    override val recommendedConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        connectionsInteractor.getRecommendedConnections().map { it.shuffled(Random(randomSeed)) }
    }
    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> by ReConsumeWhenAccountChangedConflatedBroadcastChannel {
        connectionsInteractor.getConnectedConnections().map { it.shuffled(Random(randomSeed)) }
    }

    override fun connect(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(account.id))
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

    private var sendPhoneContactsJob: Job? = null
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onContactPermissionsGranted() {
        sendPhoneContactsJob?.cancel()
        sendPhoneContactsJob = handleException {
            connectionsInteractor.sendPhoneContacts()
        }
    }
}