package com.mnassa.screen.connections.newrequests

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 9.03.2018.
 */
interface NewRequestsViewModel : MnassaViewModel {
    val newConnectionRequestsChannel: BroadcastChannel<List<ShortAccountModel>>

    fun accept(account: ShortAccountModel)
    fun decline(account: ShortAccountModel)
    suspend fun getDisconnectTimeoutDays(): Int
}