package com.mnassa.domain.interactor.impl

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.ConnectionsRepository
import com.mnassa.domain.repository.ContactsRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
class ConnectionsInteractorImpl(private val phoneContactsRepository: ContactsRepository,
                                private val connectionsRepository: ConnectionsRepository) : ConnectionsInteractor {


    override suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getRecommendedConnections()

    override suspend fun getRecommendedConnectionsWithGrouping(): ReceiveChannel<RecommendedConnections> =
            connectionsRepository.getRecommendedConnectionsWithGrouping()

    override suspend fun getConnectionRequests(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getConnectionRequests()

    override suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getConnectedConnections()

    override suspend fun getSentConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getSentConnections()

    override suspend fun getDisconnectedConnections(): ReceiveChannel<List<DeclinedShortAccountModel>> =
            connectionsRepository.getDisconnectedConnections()

    override suspend fun getMutedConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getMutedConnections()

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun sendPhoneContacts() {
        val deviceContacts = phoneContactsRepository.getPhoneContacts()

        if (deviceContacts.isNotEmpty()) {
            connectionsRepository.sendContacts(deviceContacts.map { it.phoneNumber })
        }
    }

    override suspend fun actionConnect(userAccountIds: List<String>) = connectionsRepository.actionConnect(userAccountIds)

    override suspend fun actionAccept(userAccountIds: List<String>) = connectionsRepository.actionAccept(userAccountIds)

    override suspend fun actionDecline(userAccountIds: List<String>) = connectionsRepository.actionDecline(userAccountIds)

    override suspend fun actionDisconnect(userAccountIds: List<String>) = connectionsRepository.actionDisconnect(userAccountIds)

    override suspend fun actionMute(userAccountIds: List<String>) = connectionsRepository.actionMute(userAccountIds)

    override suspend fun actionUnMute(userAccountIds: List<String>) = connectionsRepository.actionUnMute(userAccountIds)

    override suspend fun actionRevoke(userAccountIds: List<String>) = connectionsRepository.actionRevoke(userAccountIds)
}