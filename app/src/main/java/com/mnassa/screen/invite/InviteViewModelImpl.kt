package com.mnassa.screen.invite

import android.Manifest
import android.annotation.SuppressLint
import android.support.annotation.RequiresPermission
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PhoneContact
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */
class InviteViewModelImpl(
        private val connectionsInteractor: ConnectionsInteractor,
        private val inviteInteractor: InviteInteractor,
        private val userProfileInteractor: UserProfileInteractor
) : MnassaViewModelImpl(), InviteViewModel {

    override val subscribeToInvitesChannel: BroadcastChannel<Int> = BroadcastChannel(10)
    override val phoneContactChannel: BroadcastChannel<List<PhoneContact>> = BroadcastChannel(10)
    override val phoneSelectedChannel: BroadcastChannel<PhoneContact> = BroadcastChannel(10)
    override val checkPhoneContactChannel: BroadcastChannel<Boolean> = BroadcastChannel(10)

    private var retrievePhoneJob: Job? = null
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun retrievePhoneContacts() {
        retrievePhoneJob?.cancel()
        retrievePhoneJob = handleException {
            val contacts = connectionsInteractor.retrivePhoneContacts()
            phoneContactChannel.send(contacts)
        }
    }

    private var selectPhoneJob: Job? = null
    override fun selectPhoneContact(contact: PhoneContact) {
        selectPhoneJob?.cancel()
        selectPhoneJob = launchCoroutineUI {
            phoneSelectedChannel.send(contact)
        }
    }

    private var checkPhoneContactJob: Job? = null
    override fun checkPhoneContact(contact: PhoneContact) {
        checkPhoneContactJob?.cancel()
        checkPhoneContactJob = handleException {
            withProgressSuspend{
                inviteInteractor.inviteContact(contact)
                checkPhoneContactChannel.send(true)
            }
        }
    }

    private var subscribeToInvitesJob: Job? = null
    override fun subscribeToInvites() {
        subscribeToInvitesJob?.cancel()
        subscribeToInvitesJob = handleException {
            userProfileInteractor.getCurrentUserWithChannel().consumeEach {
                subscribeToInvitesChannel.send(it.invites)
            }
        }
    }

}