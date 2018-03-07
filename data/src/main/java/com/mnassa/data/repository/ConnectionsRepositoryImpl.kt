package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.ConnectionActionRequest
import com.mnassa.data.network.bean.retrofit.request.SendContactsRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.ConnectionsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/5/2018.
 */
class ConnectionsRepositoryImpl(
        private val api: FirebaseInviteApi,
        private val exceptionHandler: ExceptionHandler,
        private val databaseReference: DatabaseReference,
        private val userRepository: UserRepository,
        private val converter: ConvertersContext) : ConnectionsRepository {

    override suspend fun sendContacts(phoneNumbers: List<String>) {
        api.sendContacts(SendContactsRequest(phoneNumbers)).handleException(exceptionHandler)
    }

    override suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_RECOMMENDED)
    }

    override suspend fun getRequestedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_REQUESTED)
    }

    override suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_CONNECTED)
    }

    override suspend fun connect(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.CONNECT, userAccountIds))
                .handleException(exceptionHandler)
    }

    private fun getConnections(columnName: String): ReceiveChannel<List<ShortAccountModel>> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS)
                .child(requireNotNull(userRepository.getAccountId()))
                .child(columnName)
                .toListChannel<ShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, ShortAccountModel::class.java) }
    }
}