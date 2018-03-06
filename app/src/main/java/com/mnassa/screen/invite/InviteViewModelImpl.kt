package com.mnassa.screen.invite

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/5/2018.
 */
class InviteViewModelImpl(private val inviteInteractor: InviteInteractor) : MnassaViewModelImpl(), InviteViewModel {
    override val usersToInviteChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    private var sendPhoneContactsJob: Job? = null
    private var inviteUsersJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            inviteInteractor.getPhoneConnections().consumeEach {
                usersToInviteChannel.send(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onContactPermissionsGranted() {
        sendPhoneContactsJob?.cancel()
        sendPhoneContactsJob = handleException {
            inviteInteractor.sendPhoneContacts()
        }
    }

    override fun inviteUsers(accountIds: List<String>) {
        inviteUsersJob = handleException {
            withProgressSuspend {
                inviteInteractor.inviteUsers(accountIds)
            }
        }
    }
}