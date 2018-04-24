package com.mnassa.domain.interactor.impl

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.RecommendedConnectionsImpl
import com.mnassa.domain.repository.ConnectionsRepository
import com.mnassa.domain.repository.ContactsRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/5/2018.
 */
class ConnectionsInteractorImpl(private val phoneContactsRepository: ContactsRepository,
                                private val connectionsRepository: ConnectionsRepository,
                                private val userProfileInteractor: UserProfileInteractor) : ConnectionsInteractor {


    override suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getRecommendedConnections().withoutMe()

    override suspend fun getRecommendedConnectionsWithGrouping(): ReceiveChannel<RecommendedConnections> =
            connectionsRepository.getRecommendedConnectionsWithGrouping().withoutMeRecommended()

    override suspend fun getConnectionRequests(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getConnectionRequests().withoutMe()

    override suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getConnectedConnections().withoutMe()

    override suspend fun getSentConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getSentConnections().withoutMe()

    override suspend fun getDisconnectedConnections(): ReceiveChannel<List<DeclinedShortAccountModel>> =
            connectionsRepository.getDisconnectedConnections().withoutMe()

    override suspend fun getMutedConnections(): ReceiveChannel<List<ShortAccountModel>> =
            connectionsRepository.getMutedConnections().withoutMe()

    override suspend fun getStatusesConnections(userAccountId: String): ReceiveChannel<ConnectionStatus> =
            connectionsRepository.getStatusConnections(userAccountId)

    override suspend fun getConnectionStatusById(userAccountId: String): ConnectionStatus =
            connectionsRepository.getConnectionStatusById(userAccountId)

    override suspend fun actionConnectionStatus(connectionAction: ConnectionAction, aids: List<String>) {
        connectionsRepository.actionConnectionStatus(connectionAction, aids)
    }

    override suspend fun getDisconnectTimeoutDays(): Int = connectionsRepository.getDisconnectTimeoutDays()

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun sendPhoneContacts() {
        val deviceContacts = phoneContactsRepository.getPhoneContacts()

        if (deviceContacts.isNotEmpty()) {
            connectionsRepository.sendContacts(deviceContacts.map { it.phoneNumber })
        }
    }

    private suspend fun <T> ReceiveChannel<List<T>>.withoutMe(): ReceiveChannel<List<T>> where T : ShortAccountModel {
        return map {
            val currentUserId = userProfileInteractor.getAccountIdOrNull()
            it.filter { it.id != currentUserId }
        }
    }

    private suspend fun ReceiveChannel<RecommendedConnections>.withoutMeRecommended(): ReceiveChannel<RecommendedConnections> {
        return map {
            val currentUserId = userProfileInteractor.getAccountIdOrNull()

            RecommendedConnectionsImpl(
                    byPhone = it.byPhone.mapValues { it.value.filter { it.id != currentUserId } },
                    byEvents = it.byEvents.mapValues { it.value.filter { it.id != currentUserId } },
                    byGroups = it.byGroups.mapValues { it.value.filter { it.id != currentUserId } }
            )
        }
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun retrievePhoneContacts(): List<PhoneContact> = phoneContactsRepository.getPhoneContacts()

    override suspend fun actionConnect(userAccountIds: List<String>) = connectionsRepository.actionConnect(userAccountIds)
    override suspend fun actionAccept(userAccountIds: List<String>) = connectionsRepository.actionAccept(userAccountIds)
    override suspend fun actionDecline(userAccountIds: List<String>) = connectionsRepository.actionDecline(userAccountIds)
    override suspend fun actionDisconnect(userAccountIds: List<String>) = connectionsRepository.actionDisconnect(userAccountIds)
    override suspend fun actionMute(userAccountIds: List<String>) = connectionsRepository.actionMute(userAccountIds)
    override suspend fun actionUnMute(userAccountIds: List<String>) = connectionsRepository.actionUnMute(userAccountIds)
    override suspend fun actionRevoke(userAccountIds: List<String>) = connectionsRepository.actionRevoke(userAccountIds)
}