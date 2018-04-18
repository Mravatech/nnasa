package com.mnassa.screen.buildnetwork

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/5/2018.
 */
class BuildNetworkViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), BuildNetworkViewModel {
    override val usersToInviteChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val openScreenChannel: ArrayBroadcastChannel<BuildNetworkViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)

    private var sendPhoneContactsJob: Job? = null
    private var inviteUsersJob: Job? = null
    private var isPhonesWereSent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
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
        sendPhoneContactsJob = handleException {
            connectionsInteractor.sendPhoneContacts()
            isPhonesWereSent = true

            if (connectionsInteractor.getRecommendedConnections().receive().isEmpty()) {
                finish()
            }
        }
    }

    override fun inviteUsers(accountIds: List<String>) {
        inviteUsersJob = handleException {
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