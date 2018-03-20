package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.repository.InviteRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteInteractorImpl(private val inviteRepository: InviteRepository) : InviteInteractor {
    override suspend fun inviteContact(phoneContact: PhoneContact) {
        inviteRepository.inviteContact(phoneContact)
    }
}