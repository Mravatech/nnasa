package com.mnassa.screen.wallet

import com.mnassa.R
import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.extensions.formattedName
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 6/12/2018.
 */
class GroupWalletViewModelImpl(private val group: GroupModel, private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), WalletViewModel {
    private val groupId = group.id
    override val currentBalanceChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val spentPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val gainedPointsChannel: ConflatedBroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val transactionsChannel: ConflatedBroadcastChannel<List<TransactionModel>> = ConflatedBroadcastChannel()
    override val screenTitleChannel: BroadcastChannel<String> = ConflatedBroadcastChannel(fromDictionary(R.string.wallet_title_group).format(group.formattedName))

    override val createTransaction: BroadcastChannel<TransactionSideModel> = BroadcastChannel(1)

    override fun createTransaction() {
        launchWorker {
            val model = TransactionSideModel(group)
            createTransaction.send(model)
        }
    }

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            walletInteractor.getGroupBalance(groupId).consumeTo(currentBalanceChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getGroupSpentPointsCount(groupId).consumeTo(spentPointsChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getGroupGainedPointsCount(groupId).consumeTo(gainedPointsChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getGroupTransactions(groupId).consumeTo(transactionsChannel)
        }
    }
}