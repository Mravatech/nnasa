package com.mnassa.domain.interactor

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mnassa.domain.model.*
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface ConnectionsInteractor {

    suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getRecommendedConnectionsWithGrouping(): ReceiveChannel<RecommendedConnections>
    suspend fun getConnectionRequests(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getSentConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getDisconnectedConnections(): ReceiveChannel<List<DeclinedShortAccountModel>>
    suspend fun getMutedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getStatusesConnections(userAccountId: String): ReceiveChannel<ConnectionStatus>
    suspend fun getConnectionStatusById(userAccountId: String): ConnectionStatus

    suspend fun getDisconnectTimeoutDays(): Int
    suspend fun actionConnectionStatus(connectionAction: ConnectionAction, aids: List<String>)

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    suspend fun sendPhoneContacts()

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    suspend fun retrievePhoneContacts(): List<PhoneContact>

    suspend fun actionConnect(userAccountIds: List<String>)
    suspend fun actionAccept(userAccountIds: List<String>)
    suspend fun actionDecline(userAccountIds: List<String>)
    suspend fun actionDisconnect(userAccountIds: List<String>)
    suspend fun actionMute(userAccountIds: List<String>)
    suspend fun actionUnMute(userAccountIds: List<String>)
    suspend fun actionRevoke(userAccountIds: List<String>)
}