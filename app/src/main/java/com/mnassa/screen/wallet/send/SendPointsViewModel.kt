package com.mnassa.screen.wallet.send

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel

/**
 * Created by Peter on 4/2/2018.
 */
interface SendPointsViewModel : MnassaViewModel {
    fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?)
}