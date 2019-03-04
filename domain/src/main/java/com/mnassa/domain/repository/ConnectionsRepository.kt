package com.mnassa.domain.repository

import com.mnassa.domain.model.*
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface ConnectionsRepository {
    suspend fun sendContacts(phoneNumbers: List<String>)
    suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getRecommendedConnectionsWithGrouping(): ReceiveChannel<RecommendedConnections>
    suspend fun getConnectionRequests(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getSentConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getDisconnectedConnections(): ReceiveChannel<List<DeclinedShortAccountModel>>
    suspend fun getMutedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getStatusConnections(userAccountId: String): ReceiveChannel<ConnectionStatus>
    suspend fun getDisconnectTimeoutDays(): Int
    suspend fun getConnectionStatusById(userAccountId: String): ConnectionStatus

    suspend fun actionConnectionStatus(connectionAction: ConnectionAction, aids: List<String>)
    suspend fun actionConnect(userAccountIds: List<String>)
    suspend fun actionAccept(userAccountIds: List<String>)
    suspend fun actionDecline(userAccountIds: List<String>)
    suspend fun actionDisconnect(userAccountIds: List<String>)
    suspend fun actionMute(userAccountIds: List<String>)
    suspend fun actionUnMute(userAccountIds: List<String>)
    suspend fun actionRevoke(userAccountIds: List<String>)
}