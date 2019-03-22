package com.mnassa.screen.connections.recommended

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 09.03.2018.
 */
class RecommendedConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) :
        MnassaViewModelImpl(), RecommendedConnectionsViewModel {

    override val recommendedConnectionsChannel: ConflatedBroadcastChannel<RecommendedConnections> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getRecommendedConnectionsWithGrouping().consumeEach {
                recommendedConnectionsChannel.send(it)
            }
        }
    }

    override fun connect(accountModel: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(accountModel.id))
            }
        }
    }

    private var sendPhoneContactsJob: Job? = null
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onContactPermissionsGranted() {
        sendPhoneContactsJob?.cancel()
        sendPhoneContactsJob = launchWorker {
            connectionsInteractor.sendPhoneContacts()
        }
    }
}