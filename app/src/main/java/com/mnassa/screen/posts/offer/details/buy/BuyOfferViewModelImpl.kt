package com.mnassa.screen.posts.offer.details.buy

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consume

/**
 * Created by Peter on 5/2/2018.
 */
class BuyOfferViewModelImpl(private val walletInteractor: WalletInteractor,
                            private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), BuyOfferViewModel {
    override val resultListenerChannel: BroadcastChannel<BuyOfferViewModel.SendPointsResult> = BroadcastChannel(1)

    override fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?) {
        resolveExceptions {
            withProgressSuspend {
                val account = userProfileInteractor.getAccountByIdChannel(userProfileInteractor.getAccountIdOrException()).consume { receive() }
                        ?: return@withProgressSuspend
                val senderSide = TransactionSideModel(account)
                val recipientSide = TransactionSideModel(recipient)

                walletInteractor.sendPoints(amount, senderSide, recipientSide, description)
                resultListenerChannel.send(BuyOfferViewModel.SendPointsResult(amount, recipientSide, description))
            }
        }
    }
}