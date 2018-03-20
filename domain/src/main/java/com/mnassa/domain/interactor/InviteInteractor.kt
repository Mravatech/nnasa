package com.mnassa.domain.interactor

import com.mnassa.domain.model.PhoneContact

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

interface InviteInteractor{
    suspend fun inviteContact(phoneContact: PhoneContact)

}