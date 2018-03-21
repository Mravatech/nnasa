package com.mnassa.domain.repository

import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */
interface InviteRepository {
    suspend fun inviteContact(phoneContact: PhoneContact)
    suspend fun getInvitedContacts(userId: String): List<PhoneContactInvited>
}