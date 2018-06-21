package com.mnassa.screen.posts.offer.details.buy

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.domain.model.formattedName
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.consume

/**
 * Created by Peter on 5/2/2018.
 */
class BuyOfferViewModelImpl(private val walletInteractor: WalletInteractor,
                            private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), BuyOfferViewModel {
    override val resultListenerChannel: ArrayBroadcastChannel<BuyOfferViewModel.SendPointsResult> = ArrayBroadcastChannel(1)

    override fun sendPoints(amount: Long, recipient: ShortAccountModel, description: String?) {
        handleException {
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