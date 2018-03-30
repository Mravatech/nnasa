package com.mnassa.screen.wallet

import com.mnassa.domain.model.TransactionModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/29/2018.
 */
interface WalletViewModel : MnassaViewModel {
    val currentBalanceChannel: BroadcastChannel<Long>
    val spentPointsChannel: BroadcastChannel<Long>
    val gainedPointsChannel: BroadcastChannel<Long>
    val transactionsChannel: BroadcastChannel<List<TransactionModel>>
}