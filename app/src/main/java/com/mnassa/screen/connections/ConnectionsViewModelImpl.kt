package com.mnassa.screen.connections

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import java.util.*

/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ConnectionsViewModel {
    override val newConnectionRequestsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val recommendedConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    private var sendPhoneContactsJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val randomSeed = System.currentTimeMillis()

        handleException {
            connectionsInteractor.getConnectionRequests().consumeEach {
                val random = Random(randomSeed)
                newConnectionRequestsChannel.send(it.shuffled(random))
            }
        }

        handleException {
            connectionsInteractor.getRecommendedConnections().consumeEach {
                val random = Random(randomSeed)
                recommendedConnectionsChannel.send(it.shuffled(random))
            }
        }

        handleException {
            connectionsInteractor.getConnectedConnections().consumeEach {
                val random = Random(randomSeed)
                allConnectionsChannel.send(it.shuffled(random))
            }
        }
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

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onContactPermissionsGranted() {
        sendPhoneContactsJob?.cancel()
        sendPhoneContactsJob = handleException {
            connectionsInteractor.sendPhoneContacts()
        }
    }
}