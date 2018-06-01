package com.mnassa.screen.posts.offer.details.buy

import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 5/2/2018.
 */
class BuyOfferViewModelImpl(private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), BuyOfferViewModel {
    override val resultListenerChannel: ArrayBroadcastChannel<BuyOfferViewModel.SendPointsResult> = ArrayBroadcastChannel(1)

    override fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?) {
        handleException {
            withProgressSuspend {
                walletInteractor.sendPoints(amount, recipient.id, description)
                resultListenerChannel.send(BuyOfferViewModel.SendPointsResult(amount, recipient, description))
            }
        }
    }
}