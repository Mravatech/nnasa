package com.mnassa.data.repository

import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.repository.CountersRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filterNotNull

/**
 * Created by Peter on 3/7/2018.
 */
class CountersRepositoryImpl(
    private val db: DatabaseReference,
    private val userRepository: UserRepository,
    private val exceptionHandler: ExceptionHandler
) : CountersRepository {

    override suspend fun produceNumberOfCommunities(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_COMMUNITIES)

    override suspend fun produceNumberOfConnections(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_CONNECTIONS)

    override suspend fun produceNumberOfDisconnected(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_DISCONNECTED)

    override suspend fun produceNumberOfRecommendations(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_RECOMMENDATIONS)

    override suspend fun produceNumberOfRequested(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_REQUESTED)

    override suspend fun produceNumberOfSent(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_SENT)

    override suspend fun produceNumberOfUnreadChats(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_CHATS)

    override suspend fun produceNumberOfUnreadEvents(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_EVENTS)

    override suspend fun produceNumberOfUnreadNeeds(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_NEEDS)

    override suspend fun produceNumberOfUnreadNotifications(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_NOTIFICATIONS)

    override suspend fun produceNumberOfUnreadResponses(): ReceiveChannel<Int> =
        produceNumberOf(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_RESPONSES)

    private suspend fun produceNumberOf(key: String): ReceiveChannel<Int> {
        val accountId = userRepository.getAccountIdOrException()
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
            .child(accountId)
            .child(key)
            .toValueChannel<Int>(exceptionHandler)
            .filterNotNull()
    }

}