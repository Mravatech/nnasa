package com.mnassa.screen.hail

import android.Manifest
import android.annotation.SuppressLint
import android.support.annotation.RequiresPermission
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.PhoneContact
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */
class InviteToMnassaViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), InviteToMnassaViewModel {
    override val phoneContactChannel: BroadcastChannel<List<PhoneContact>> = BroadcastChannel(10)
    override val phoneSelectedChannel: BroadcastChannel<PhoneContact> = BroadcastChannel(10)

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
    override fun selectPhoneContact(contact: PhoneContact){
        selectPhoneJob?.cancel()
        selectPhoneJob = launchCoroutineUI {
            phoneSelectedChannel.send(contact)
        }
    }

}