package com.mnassa.screen.invite

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContact
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */
class InviteViewModelImpl(
        private val connectionsInteractor: ConnectionsInteractor,
        private val inviteInteractor: InviteInteractor
) : MnassaViewModelImpl(), InviteViewModel {
    override val inviteRewardChannel: BroadcastChannel<Long?> = ConflatedBroadcastChannel()
    override val invitesCountChannel: BroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val phoneContactChannel: BroadcastChannel<List<PhoneContact>> = ConflatedBroadcastChannel()
    override val checkPhoneContactChannel: BroadcastChannel<Boolean> = BroadcastChannel(10)

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            inviteInteractor.getInvitesCountChannel().consumeTo(invitesCountChannel)
        }
        setupScope.launchWorker {
            inviteInteractor.getRewardPerInvite().consumeTo(inviteRewardChannel)
        }
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun retrievePhoneContacts() {
        launchWorker {
            val contacts = connectionsInteractor.retrievePhoneContacts()
            phoneContactChannel.send(contacts)
        }
    }

    override fun checkPhoneContact(contact: PhoneContact) {
        launchWorker {
            withProgressSuspend {
                val inviteSourceHolder: InviteSourceHolder by instance()
                val inviteSource = inviteSourceHolder.source
                when (inviteSource) {
                    is InviteSource.Post -> inviteInteractor.inviteContactToPost(contact, inviteSource.post.id)
                    is InviteSource.Event -> inviteInteractor.inviteContactToEvent(contact, inviteSource.event.id)
                    is InviteSource.Group -> inviteInteractor.inviteContactToGroup(contact, inviteSource.group.id)
                    else -> inviteInteractor.inviteContact(contact)
                }

                checkPhoneContactChannel.send(true)
            }
        }
    }
}