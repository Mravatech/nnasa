package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.api.FirebaseWalletApi
import com.mnassa.data.network.bean.firebase.TransactionDbEntity
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.repository.WalletRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/30/2018.
 */
class WalletRepositoryImpl(
        private val userRepository: UserRepository,
        private val converter: ConvertersContext,
        private val exceptionHandler: ExceptionHandler,
        private val db: DatabaseReference,
        private val walletApi: FirebaseWalletApi
) : WalletRepository {

    override suspend fun getBalance(): ReceiveChannel<Long> {
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountId())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_POINTS)
                .toValueChannel<Long>(exceptionHandler)
                .map { it ?: 0 }
    }

    override suspend fun getSpentPointsCount(): ReceiveChannel<Long> {
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountId())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_TOTAL_OUTCOME)
                .toValueChannel<Long>(exceptionHandler)
                .map { it ?: 0 }
    }

    override suspend fun getGainedPointsCount(): ReceiveChannel<Long> {
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountId())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_TOTAL_INCOME)
                .toValueChannel<Long>(exceptionHandler)
                .map { it ?: 0 }
    }

    override suspend fun getTransactions(): ReceiveChannel<List<TransactionModel>> {
        return db.child(DatabaseContract.TABLE_TRANSACTIONS)
                .child(userRepository.getAccountId())
                .toListChannel<TransactionDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, TransactionModel::class.java) }
    }
}