package com.mnassa.domain.interactor

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

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

    suspend fun getDisconnectTimeoutDays(): Int

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    suspend fun sendPhoneContacts()
    suspend fun retrievePhoneContacts():List<PhoneContact>

    suspend fun actionConnect(userAccountIds: List<String>)
    suspend fun actionAccept(userAccountIds: List<String>)
    suspend fun actionDecline(userAccountIds: List<String>)
    suspend fun actionDisconnect(userAccountIds: List<String>)
    suspend fun actionMute(userAccountIds: List<String>)
    suspend fun actionUnMute(userAccountIds: List<String>)
    suspend fun actionRevoke(userAccountIds: List<String>)
}