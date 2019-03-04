package com.mnassa.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.mnassa.core.converter.ConvertersContext
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseConnectionsApi
import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.bean.firebase.ConnectionDbStatuses
import com.mnassa.data.network.bean.firebase.DeclinedShortAccountDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.ConnectionActionRequest
import com.mnassa.data.network.bean.retrofit.request.ConnectionStatusRequest
import com.mnassa.data.network.bean.retrofit.request.SendContactsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.RecommendedConnectionsImpl
import com.mnassa.domain.repository.ConnectionsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.produce
import java.util.*

/**
 * Created by Peter on 3/5/2018.
 */
class ConnectionsRepositoryImpl(
        private val api: FirebaseInviteApi,
        private val exceptionHandler: ExceptionHandler,
        private val databaseReference: DatabaseReference,
        private val userRepository: UserRepository,
        private val firebaseConnectionsApi: FirebaseConnectionsApi,
        private val converter: ConvertersContext) : ConnectionsRepository {

    private var connectionsCache = WeakHashMap<String, List<ShortAccountModel>>()

    override suspend fun sendContacts(phoneNumbers: List<String>) {
        val phonesToSend = phoneNumbers.map { it.replace(" ", "") }.filter { it.isNotBlank() }
        api.sendContacts(SendContactsRequest(phonesToSend)).handleException(exceptionHandler)
    }

    override suspend fun getRecommendedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_RECOMMENDED)
    }

    override suspend fun getRecommendedConnectionsWithGrouping(): ReceiveChannel<RecommendedConnections> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS_RECOMMENDED)
                .child(requireNotNull(userRepository.getAccountIdOrException()))
                .toValueChannel<DataSnapshot>(exceptionHandler)
                .map {
                    if (it == null) RecommendedConnectionsImpl(mapOf())
                    else converter.convert(it, RecommendedConnections::class.java)
                }
    }

    override suspend fun actionConnectionStatus(connectionAction: ConnectionAction, aids: List<String>) {
        firebaseConnectionsApi.connectionAction(ConnectionStatusRequest(getConnectionAction(connectionAction), aids))
                .handleException(exceptionHandler)
    }

    override suspend fun getConnectionRequests(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_REQUESTED)
    }

    override suspend fun getConnectedConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return GlobalScope.produce(Dispatchers.Unconfined) {
            val accountId = userRepository.getAccountIdOrException()
            connectionsCache[accountId]?.takeIf { it.isNotEmpty() }?.let { send(it) }

            getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_CONNECTED).consumeEach {
                send(it)
                connectionsCache[accountId] = it
            }
        }
    }

    override suspend fun getSentConnections(): ReceiveChannel<List<ShortAccountModel>> {
        return getConnections(DatabaseContract.TABLE_CONNECTIONS_COL_SENT)
    }

    override suspend fun getStatusConnections(userAccountId: String): ReceiveChannel<ConnectionStatus> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS)
                .child(requireNotNull(userRepository.getAccountIdOrException()))
                .child(DatabaseContract.TABLE_CONNECTIONS_COL_STATUSES)
                .child(userAccountId)
                .toValueChannel<ConnectionDbStatuses>(exceptionHandler)
                .map {
                    converter.convert(it?.connectionsStatus ?: "", ConnectionStatus::class.java)
                }
    }

    override suspend fun getConnectionStatusById(userAccountId: String): ConnectionStatus {
        val status = databaseReference.child(DatabaseContract.TABLE_CONNECTIONS)
                .child(requireNotNull(userRepository.getAccountIdOrException()))
                .child(DatabaseContract.TABLE_CONNECTIONS_COL_STATUSES)
                .child(userAccountId)
                .await<ConnectionDbStatuses>(exceptionHandler)
        return converter.convert(status?.connectionsStatus ?: "", ConnectionStatus::class.java)
    }

    override suspend fun getDisconnectedConnections(): ReceiveChannel<List<DeclinedShortAccountModel>> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS)
                .child(requireNotNull(userRepository.getAccountIdOrException()))
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

    private suspend fun getConnections(columnName: String): ReceiveChannel<List<ShortAccountModel>> {
        return databaseReference.child(DatabaseContract.TABLE_CONNECTIONS)
                .child(userRepository.getAccountIdOrException())
                .child(columnName)
                .toListChannel<ShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, ShortAccountModel::class.java) }
    }

    private fun getConnectionAction(action: ConnectionAction) =
            when (action) {
                ConnectionAction.CONNECT -> NetworkContract.ConnectionAction.CONNECT
                ConnectionAction.ACCEPT -> NetworkContract.ConnectionAction.ACCEPT
                ConnectionAction.DECLINE -> NetworkContract.ConnectionAction.DECLINE
                ConnectionAction.DISCONNECT -> NetworkContract.ConnectionAction.DISCONNECT
                ConnectionAction.MUTE -> NetworkContract.ConnectionAction.MUTE
                ConnectionAction.UN_MUTE -> NetworkContract.ConnectionAction.UN_MUTE
                ConnectionAction.REVOKE -> NetworkContract.ConnectionAction.REVOKE
                else -> throw IllegalArgumentException("No Such ConnectionStatus Type")
            }

}
