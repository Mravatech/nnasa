package com.mnassa.domain.interactor.impl

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.ConnectionsRepository
import com.mnassa.domain.repository.ContactsRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
class ConnectionsInteractorImpl(private val phoneContactsRepository: ContactsRepository,
                                private val connectionsRepository: ConnectionsRepository) : ConnectionsInteractor {


    override suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return connectionsRepository.getRecommendedConnections()
    }

    override suspend fun getRequestedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return connectionsRepository.getRequestedConnections()
    }

    override suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return connectionsRepository.getConnectedConnections()
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun sendPhoneContacts() {
        val deviceContacts = phoneContactsRepository.getPhoneContacts()

        if (deviceContacts.isNotEmpty()) {
            connectionsRepository.sendContacts(deviceContacts.map { it.phoneNumber })
        }
    }

    override suspend fun inviteUsers(userAccountIds: List<String>) {
        connectionsRepository.connect(userAccountIds)
    }
}