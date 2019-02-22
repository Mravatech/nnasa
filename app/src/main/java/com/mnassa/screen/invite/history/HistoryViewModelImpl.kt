package com.mnassa.screen.invite.history

import android.os.Bundle
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

class HistoryViewModelImpl(
    private val inviteInteractor: InviteInteractor
) : MnassaViewModelImpl(), HistoryViewModel {

    override val phoneContactChannel: BroadcastChannel<List<PhoneContactInvited>> =
        ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showProgress()

        resolveExceptions {
            inviteInteractor.getInvitedContacts().consumeEach {
                phoneContactChannel.send(it)
                hideProgress()
            }
        }
    }
}