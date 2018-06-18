package com.mnassa.screen.connections.select

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
interface SelectConnectionViewModel : MnassaViewModel {
    val allConnectionsChannel: BroadcastChannel<List<ShortAccountModel>>

    sealed class AdditionalData {
        class Empty : AdditionalData()
        class IncludeGroupMembers(val groupId: String) : AdditionalData()
    }
}