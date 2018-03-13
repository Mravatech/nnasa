package com.mnassa.screen.connections.archived

import com.mnassa.domain.model.DeclinedShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 9.03.2018.
 */
interface ArchivedConnectionViewModel : MnassaViewModel {
    val declinedConnectionsChannel: BroadcastChannel<List<DeclinedShortAccountModel>>
    suspend fun getDisconnectTimeoutDays(): Int

    fun connect(account: DeclinedShortAccountModel)
}