package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.api.FirebaseWalletApi
import com.mnassa.data.network.bean.firebase.RewardDbEntity
import com.mnassa.data.network.bean.firebase.TransactionDbEntity
import com.mnassa.data.network.bean.retrofit.request.RewardForCommentRequest
import com.mnassa.data.network.bean.retrofit.request.SendPointsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.RewardModel
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
                .child(userRepository.getAccountIdOrException())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_POINTS)
                .toValueChannel<Long>(exceptionHandler)
                .map { it ?: 0 }
    }

    override suspend fun getSpentPointsCount(): ReceiveChannel<Long> {
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountIdOrException())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_TOTAL_OUTCOME)
                .toValueChannel<Long>(exceptionHandler)
                .map { it ?: 0 }
    }

    override suspend fun getGainedPointsCount(): ReceiveChannel<Long> {
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountIdOrException())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_TOTAL_INCOME)
                .toValueChannel<Long>(exceptionHandler)
                .map { it ?: 0 }
    }

    override suspend fun getTransactions(): ReceiveChannel<List<TransactionModel>> {
        return db.child(DatabaseContract.TABLE_TRANSACTIONS)
                .child(userRepository.getAccountIdOrException())
                .toListChannel<TransactionDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, TransactionModel::class.java) }
    }

    override suspend fun getDefaultRewardingPointsCount(): Int {
        return db.child(DatabaseContract.TABLE_DICTIONARY)
                .child(DatabaseContract.TABLE_DICTIONARY_COL_REWARD_FOR_COMMENT)
                .await<RewardDbEntity>(exceptionHandler)!!
                .amount
    }


    override suspend fun sendPoints(amount: Long, recipientId: String, description: String?) {
        walletApi.sendPoints(SendPointsRequest(
                fromAid = requireNotNull(userRepository.getAccountIdOrException()),
                toAid = recipientId,
                amount = amount,
                userDescription = description
        )).handleException(exceptionHandler)
    }

    override suspend fun sendPointsForComment(rewardModel: RewardModel) {
        walletApi.rewardForComment(RewardForCommentRequest(
                fromAid = requireNotNull(userRepository.getAccountIdOrException()),
                toAid = rewardModel.recipientId,
                amount = rewardModel.amount,
                userDescription = rewardModel.userDescription,
                commentId = rewardModel.commentId
        )).handleException(exceptionHandler)
    }
}