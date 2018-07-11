package com.mnassa.screen.invite

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContact
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
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

    override val invitesCountChannel: BroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val phoneContactChannel: BroadcastChannel<List<PhoneContact>> = ConflatedBroadcastChannel()
    override val checkPhoneContactChannel: BroadcastChannel<Boolean> = BroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            inviteInteractor.getInvitesCountChannel().consumeEach {
                invitesCountChannel.send(it)
            }
        }
    }

    private var retrievePhoneJob: Job? = null
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun retrievePhoneContacts() {
        retrievePhoneJob?.cancel()
        retrievePhoneJob = handleException {
            val contacts = connectionsInteractor.retrievePhoneContacts()
            phoneContactChannel.send(contacts)
        }
    }

    private var checkPhoneContactJob: Job? = null
    override fun checkPhoneContact(contact: PhoneContact) {
        checkPhoneContactJob?.cancel()
        checkPhoneContactJob = handleException {
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

    override suspend fun getInviteReward(): Long? = inviteInteractor.getRewardPerInvite()
}