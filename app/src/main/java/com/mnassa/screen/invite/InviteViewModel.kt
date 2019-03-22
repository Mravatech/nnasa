package com.mnassa.screen.invite

import com.mnassa.domain.model.PhoneContact
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */
interface InviteViewModel : MnassaViewModel {
    val phoneContactChannel: BroadcastChannel<List<PhoneContact>>
    val checkPhoneContactChannel: BroadcastChannel<Boolean>
    val invitesCountChannel: BroadcastChannel<Int>
    val inviteRewardChannel: BroadcastChannel<Long?>
    fun retrievePhoneContacts()
    fun checkPhoneContact(contact: PhoneContact)
}