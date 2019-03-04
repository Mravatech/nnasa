package com.mnassa.screen.connections.sent

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 9.03.2018.
 */
interface SentConnectionsViewModel : MnassaViewModel {
    val sentConnectionsChannel: BroadcastChannel<List<ShortAccountModel>>

    fun cancelRequest(account: ShortAccountModel)
}