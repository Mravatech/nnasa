package com.mnassa.screen.wallet

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.TransactionModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/29/2018.
 */
class WalletViewModelImpl(private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), WalletViewModel {
    override val currentBalanceChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val spentPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val gainedPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val transactionsChannel: ConflatedBroadcastChannel<List<TransactionModel>> = ConflatedBroadcastChannel()

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