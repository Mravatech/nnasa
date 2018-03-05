package com.mnassa.domain.interactor.impl

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.InviteInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.ContactsRepository
import com.mnassa.domain.repository.InviteRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
class InviteInteractorImpl(private val phoneContactsRepository: ContactsRepository,
                           private val inviteRepository: InviteRepository) : InviteInteractor {


    override suspend fun getPhoneConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return inviteRepository.getRecommendedByPhoneUsers()
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun sendPhoneContacts() {
        val deviceContacts = phoneContactsRepository.getPhoneContacts()

        if (deviceContacts.isNotEmpty()) {
            inviteRepository.sendContacts(deviceContacts.map { it.phoneNumber })
        }
    }

    override suspend fun inviteUsers(userAccountIds: List<String>) {
        inviteRepository.connect(userAccountIds)
    }
}