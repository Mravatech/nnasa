package com.mnassa.screen.wallet

import android.os.Bundle
import com.mnassa.R
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.translation.fromDictionary
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

    override suspend fun getTransactionSide(): TransactionSideModel {
        return handleExceptionsSuspend {
            val user = userProfileInteractor.getProfileById(userProfileInteractor.getAccountIdOrException())
            TransactionSideModel(user!!)
        }!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            walletInteractor.getBalance().consumeTo(currentBalanceChannel)
        }

        resolveExceptions {
            walletInteractor.getSpentPointsCount().consumeTo(spentPointsChannel)
        }

        resolveExceptions {
            walletInteractor.getGainedPointsCount().consumeTo(gainedPointsChannel)
        }

        resolveExceptions {
            walletInteractor.getTransactions().consumeTo(transactionsChannel)
        }
    }
}