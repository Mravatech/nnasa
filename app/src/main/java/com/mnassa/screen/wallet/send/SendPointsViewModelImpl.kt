package com.mnassa.screen.wallet.send

import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by Peter on 4/2/2018.
 */
class SendPointsViewModelImpl(private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), SendPointsViewModel {

    override fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?) {
        handleException {
            withProgressSuspend {
                walletInteractor.sendPoints(amount, recipient.id, description)
            }
        }
    }
}