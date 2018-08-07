package com.mnassa.domain.interactor

import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

interface InviteInteractor {
    suspend fun inviteContact(phoneContact: PhoneContact)
    suspend fun inviteContactToGroup(phoneContact: PhoneContact, communityId: String)
    suspend fun inviteContactToPost(phoneContact: PhoneContact, postId: String)
    suspend fun inviteContactToEvent(phoneContact: PhoneContact, eventId: String)

    suspend fun getInvitedContacts(): List<PhoneContactInvited>
    suspend fun getInvitesCountChannel(): ReceiveChannel<Int>
    suspend fun getRewardPerInvite(): Long?
}