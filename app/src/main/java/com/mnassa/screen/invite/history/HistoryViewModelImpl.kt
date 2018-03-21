package com.mnassa.screen.invite.history

import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */

class HistoryViewModelImpl(
        private val inviteInteractor: InviteInteractor
) : MnassaViewModelImpl(), HistoryViewModel {

    override val phoneContactChannel: BroadcastChannel<List<PhoneContactInvited>> = BroadcastChannel(10)

    private var retrievePhoneJob: Job? = null
    override fun retrievePhoneContacts() {
        retrievePhoneJob?.cancel()
        retrievePhoneJob = handleException {
            withProgressSuspend{
                val contacts = inviteInteractor.getInvitedContacts()
                Timber.i(contacts.toString())
                phoneContactChannel.send(contacts)
            }
        }
    }

}