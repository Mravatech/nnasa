package com.mnassa.screen.buildnetwork

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 3/5/2018.
 */
class BuildNetworkViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), BuildNetworkViewModel {
    override val usersToInviteChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val openScreenChannel: BroadcastChannel<BuildNetworkViewModel.OpenScreenCommand> = BroadcastChannel(10)

    private var sendPhoneContactsJob: Job? = null
    private var inviteUsersJob: Job? = null
    private var isPhonesWereSent: Boolean = false

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            connectionsInteractor.getRecommendedConnections().consumeEach {
                usersToInviteChannel.send(it)

                if (it.isEmpty() && isPhonesWereSent) {
                    finish()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onContactPermissionsGranted() {
        sendPhoneContactsJob?.cancel()
        sendPhoneContactsJob = launchWorker {
            connectionsInteractor.sendPhoneContacts()
            isPhonesWereSent = true

            if (connectionsInteractor.getRecommendedConnections().consume { receive() }.isEmpty()) {
                finish()
            }
        }
    }

    override fun inviteUsers(accountIds: List<String>) {
        inviteUsersJob = launchWorker {
            withProgressSuspend {
                connectionsInteractor.actionConnect(accountIds)
            }
            finish()
        }
    }

    private suspend fun finish() {
        openScreenChannel.send(BuildNetworkViewModel.OpenScreenCommand.MainScreen())
    }
}