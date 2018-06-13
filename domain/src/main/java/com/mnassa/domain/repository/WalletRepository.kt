package com.mnassa.domain.repository

import com.mnassa.domain.model.RewardModel
import com.mnassa.domain.model.TransactionModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/30/2018.
 */
interface WalletRepository {
    suspend fun getBalance(): ReceiveChannel<Long>
    suspend fun getSpentPointsCount(): ReceiveChannel<Long>
    suspend fun getGainedPointsCount(): ReceiveChannel<Long>
    suspend fun getTransactions(): ReceiveChannel<List<TransactionModel>>
    suspend fun getDefaultRewardingPoints(): Long

    suspend fun sendPoints(amount: Long, recipientId: String, description: String?)
    suspend fun sendPointsForComment(rewardModel: RewardModel)

    suspend fun getGroupBalance(groupId: String): ReceiveChannel<Long>
    suspend fun getGroupSpentPointsCount(groupId: String): ReceiveChannel<Long>
    suspend fun getGroupGainedPointsCount(groupId: String): ReceiveChannel<Long>
    suspend fun getGroupTransactions(groupId: String): ReceiveChannel<List<TransactionModel>>
}