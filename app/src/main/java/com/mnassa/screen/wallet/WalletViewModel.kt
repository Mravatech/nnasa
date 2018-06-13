package com.mnassa.screen.wallet

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.TransactionModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import java.io.Serializable

/**
 * Created by Peter on 3/29/2018.
 */
interface WalletViewModel : MnassaViewModel {
    val currentBalanceChannel: BroadcastChannel<Long>
    val spentPointsChannel: BroadcastChannel<Long>
    val gainedPointsChannel: BroadcastChannel<Long>
    val transactionsChannel: BroadcastChannel<List<TransactionModel>>
    val screenTitleChannel: BroadcastChannel<String>

    open class WalletSource : Serializable {
        class User : WalletSource()

        class Group(val group: GroupModel) : WalletSource()
    }
}