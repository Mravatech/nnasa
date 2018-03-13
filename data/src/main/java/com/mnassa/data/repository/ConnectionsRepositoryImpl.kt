package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.bean.firebase.DeclinedShortAccountDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.ConnectionActionRequest
import com.mnassa.data.network.bean.retrofit.request.SendContactsRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.RecommendedConnectionsImpl
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

    override suspend fun getRecommendedConnectionsWithGrouping(): ReceiveChannel<RecommendedConnections> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS_RECOMMENDED)
                .child(requireNotNull(userRepository.getAccountId()))
                .toValueChannel<DataSnapshot>(exceptionHandler)
                .map {
                    if (it == null) {
                        RecommendedConnectionsImpl(mapOf(), mapOf(), mapOf())
                    } else converter.convert(it, RecommendedConnections::class.java)
                }
    }

    override suspend fun getConnectionRequests(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_REQUESTED)
    }

    override suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_CONNECTED)
    }

    override suspend fun getSentConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_SENT)
    }

    override suspend fun getDisconnectedConnections(): ReceiveChannel<List<DeclinedShortAccountModel>> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS)
                .child(requireNotNull(userRepository.getAccountId()))
                .child(DatabaseContract.TABLE_CONNECTIONS_COL_DISCONNECTED)
                .toListChannel<DeclinedShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, DeclinedShortAccountModel::class.java) }
    }

    override suspend fun getMutedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_MUTED)
    }

    override suspend fun getDisconnectTimeoutDays(): Int {
        return databaseReference.child(DatabaseContract.TABLE_CLIENT_DATA)
                .child(DatabaseContract.TABLE_CLIENT_DATA_COL_DISCONNECT_TIMEOUT)
                .await(exceptionHandler)!!
    }

    override suspend fun actionConnect(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.CONNECT, userAccountIds))
                .handleException(exceptionHandler)
    }

    override suspend fun actionAccept(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.ACCEPT, userAccountIds))
                .handleException(exceptionHandler)
    }

    override suspend fun actionDecline(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.DECLINE, userAccountIds))
                .handleException(exceptionHandler)
    }

    override suspend fun actionDisconnect(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.DISCONNECT, userAccountIds))
                .handleException(exceptionHandler)
    }

    override suspend fun actionMute(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.MUTE, userAccountIds))
                .handleException(exceptionHandler)
    }

    override suspend fun actionUnMute(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.UN_MUTE, userAccountIds))
                .handleException(exceptionHandler)
    }

    override suspend fun actionRevoke(userAccountIds: List<String>) {
        api.executeConnectionAction(ConnectionActionRequest(NetworkContract.ConnectionAction.REVOKE, userAccountIds))
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