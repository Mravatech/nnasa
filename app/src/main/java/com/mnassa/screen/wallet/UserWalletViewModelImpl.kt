package com.mnassa.screen.wallet

import android.os.Bundle
import com.mnassa.R
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/29/2018.
 */
class UserWalletViewModelImpl(private val walletInteractor: WalletInteractor, private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), WalletViewModel {
    override val currentBalanceChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val spentPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val gainedPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val transactionsChannel: ConflatedBroadcastChannel<List<TransactionModel>> = ConflatedBroadcastChannel()
    override val screenTitleChannel: BroadcastChannel<String> = ConflatedBroadcastChannel(fromDictionary(R.string.wallet_title))

    override suspend fun getTransactionSide(): TransactionSideModel {
        return handleExceptionsSuspend {
            val user = userProfileInteractor.getProfileById(userProfileInteractor.getAccountIdOrException())
            TransactionSideModel(user!!)
        }!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            walletInteractor.getBalance().consumeTo(currentBalanceChannel)
        }

        handleException {
            walletInteractor.getSpentPointsCount().consumeTo(spentPointsChannel)
        }

        handleException {
            walletInteractor.getGainedPointsCount().consumeTo(gainedPointsChannel)
        }

        handleException {
            walletInteractor.getTransactions().consumeTo(transactionsChannel)
        }
    }
}