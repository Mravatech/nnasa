package com.mnassa.screen.wallet

import com.mnassa.R
import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 3/29/2018.
 */
class UserWalletViewModelImpl(private val walletInteractor: WalletInteractor, private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), WalletViewModel {
    override val currentBalanceChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val spentPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val gainedPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val transactionsChannel: ConflatedBroadcastChannel<List<TransactionModel>> = ConflatedBroadcastChannel()
    override val screenTitleChannel: BroadcastChannel<String> = ConflatedBroadcastChannel(fromDictionary(R.string.wallet_title))

    override val createTransaction: BroadcastChannel<TransactionSideModel> = BroadcastChannel(1)

    override fun createTransaction() {
        launchWorker {
            withProgressSuspend {
                val user = userProfileInteractor.getProfileById(userProfileInteractor.getAccountIdOrException())!!
                val model = TransactionSideModel(user)
                createTransaction.send(model)
            }
        }
    }

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            walletInteractor.getBalance().consumeTo(currentBalanceChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getSpentPointsCount().consumeTo(spentPointsChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getGainedPointsCount().consumeTo(gainedPointsChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getTransactions().consumeTo(transactionsChannel)
        }
    }
}