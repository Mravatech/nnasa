package com.mnassa.domain.repository

import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface ConnectionsRepository {
    suspend fun sendContacts(phoneNumbers: List<String>)
    suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getRequestedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>>
    suspend fun connect(userAccountIds: List<String>)


}