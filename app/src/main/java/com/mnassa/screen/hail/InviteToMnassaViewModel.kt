package com.mnassa.screen.hail

import com.mnassa.domain.model.PhoneContact
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */
interface InviteToMnassaViewModel : MnassaViewModel {
    val phoneContactChannel: BroadcastChannel<List<PhoneContact>>
    val phoneSelectedChannel: BroadcastChannel<PhoneContact>
    val checkPhoneContactChannel: BroadcastChannel<Boolean>
    val subscribeToInvitesChannel: BroadcastChannel<Int>
    fun retrievePhoneContacts()
    fun selectPhoneContact(contact: PhoneContact)
    fun checkPhoneContact(contact: PhoneContact)
    fun subscribeToInvites()
}