package com.mnassa.screen.invite

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
class InviteViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), InviteViewModel {
    override val usersToInviteChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val openScreenChannel: ArrayBroadcastChannel<InviteViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)

    private var sendPhoneContactsJob: Job? = null
    private var inviteUsersJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getRecommendedConnections().consumeEach {
                usersToInviteChannel.send(it)
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

    override fun inviteUsers(accountIds: List<String>) {
        inviteUsersJob = handleException {
            withProgressSuspend {
                connectionsInteractor.actionConnect(accountIds)
            }
            openScreenChannel.send(InviteViewModel.OpenScreenCommand.MainScreen())
        }
    }
}