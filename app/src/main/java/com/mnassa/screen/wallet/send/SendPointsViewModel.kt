package com.mnassa.screen.wallet.send

import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
interface SendPointsViewModel : MnassaViewModel {
    val resultListenerChannel: BroadcastChannel<SendPointsResult>

    fun sendPoints(amount: Long, sender: TransactionSideModel, recipient: TransactionSideModel, description: String?)

    data class SendPointsResult(
            val amount: Long,
            val sender: TransactionSideModel,
            val recipient: TransactionSideModel,
            val description: String?
    )
}