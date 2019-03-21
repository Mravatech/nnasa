package com.mnassa.screen.invite.history

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

class HistoryViewModelImpl(
    private val inviteInteractor: InviteInteractor
) : MnassaViewModelImpl(), HistoryViewModel {

    override val phoneContactChannel: BroadcastChannel<List<PhoneContactInvited>> =
        ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            inviteInteractor.getInvitedContacts().consumeEach {
                phoneContactChannel.send(it)
            }
        }
    }
}