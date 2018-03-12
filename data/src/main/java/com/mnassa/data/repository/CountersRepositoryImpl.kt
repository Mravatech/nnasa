package com.mnassa.data.repository

import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.repository.CountersRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/7/2018.
 */
class CountersRepositoryImpl(private val db: DatabaseReference, private val userRepository: UserRepository, private val exceptionHandler: ExceptionHandler) : CountersRepository {
    override val numberOfCommunities: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_COMMUNITIES)
    override val numberOfConnections: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_CONNECTIONS)
    override val numberOfDisconnected: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_DISCONNECTED)
    override val numberOfRecommendations: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_RECOMMENDATIONS)
    override val numberOfRequested: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_REQUESTED)
    override val numberOfSent: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_SENT)
    override val numberOfUnreadChats: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_CHATS)
    override val numberOfUnreadEvents: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_EVENTS)
    override val numberOfUnreadNeeds: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_NEEDS)
    override val numberOfUnreadNotifications: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_NOTIFICATIONS)
    override val numberOfUnreadResponses: ReceiveChannel<Int> get() = getCounter(DatabaseContract.TABLE_ACCOUNTS_COL_NUM_UNREAD_RESPONSES)

    private fun getCounter(key: String): ReceiveChannel<Int> {
        val accountId = userRepository.getAccountId()

        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(accountId)
                .child(key)
                .toValueChannel<Int>(exceptionHandler)
                .map { requireNotNull(it) }
    }
}