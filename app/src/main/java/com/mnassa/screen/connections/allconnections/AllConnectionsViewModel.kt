package com.mnassa.screen.connections.allconnections

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 3/14/2018.
 */
interface AllConnectionsViewModel : MnassaViewModel {
    val allConnectionsChannel: BroadcastChannel<List<ShortAccountModel>>

    fun disconnect(account: ShortAccountModel)
}