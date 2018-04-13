package com.mnassa.screen.wallet.send

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
interface SendPointsViewModel : MnassaViewModel {
    val resultListenerChannel: BroadcastChannel<SendPointsResult>

    fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?)

    data class SendPointsResult(
            val amount: Long,
            val recipient: ShortAccountModel,
            val description: String?
    )
}