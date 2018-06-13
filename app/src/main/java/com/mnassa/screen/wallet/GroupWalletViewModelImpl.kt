package com.mnassa.screen.wallet

import android.os.Bundle
import com.mnassa.R
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.TransactionModel
import com.mnassa.extensions.formattedName
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 6/12/2018.
 */
class GroupWalletViewModelImpl(group: GroupModel, private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), WalletViewModel {
    private val groupId = group.id
    override val currentBalanceChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val spentPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val gainedPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val transactionsChannel: ConflatedBroadcastChannel<List<TransactionModel>> = ConflatedBroadcastChannel()
    override val screenTitleChannel: BroadcastChannel<String> = ConflatedBroadcastChannel(fromDictionary(R.string.wallet_title_group).format(group.formattedName))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            walletInteractor.getGroupBalance(groupId).consumeTo(currentBalanceChannel)
        }

        handleException {
            walletInteractor.getGroupSpentPointsCount(groupId).consumeTo(spentPointsChannel)
        }

        handleException {
            walletInteractor.getGroupGainedPointsCount(groupId).consumeTo(gainedPointsChannel)
        }

        handleException {
            walletInteractor.getGroupTransactions(groupId).consumeTo(transactionsChannel)
        }
    }
}