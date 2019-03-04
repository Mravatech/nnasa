package com.mnassa.screen.posts.offer.details.buy

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 5/2/2018.
 */
interface BuyOfferViewModel : MnassaViewModel {
    val resultListenerChannel: BroadcastChannel<SendPointsResult>

    fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?)

    data class SendPointsResult(
            val amount: Long,
            val recipient: TransactionSideModel,
            val description: String?
    )
}