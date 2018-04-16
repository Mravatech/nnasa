package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.domain.repository.InviteRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteInteractorImpl(
        private val inviteRepository: InviteRepository,
        private val userRepository: UserRepository) : InviteInteractor {

    override suspend fun inviteContact(phoneContact: PhoneContact) {
        inviteRepository.inviteContact(phoneContact)
    }

    override suspend fun getInvitedContacts(): List<PhoneContactInvited> {
        val userId = requireNotNull(userRepository.getAccountIdOrException())
        return inviteRepository.getInvitedContacts(userId)
    }

    override suspend fun getInvitesCountChannel(): ReceiveChannel<Int> {
        return inviteRepository.getInvitesCountChannel()
    }
}