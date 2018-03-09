package com.mnassa.screen.connections

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface ConnectionsViewModel : MnassaViewModel {
    val newConnectionRequestsChannel: BroadcastChannel<List<ShortAccountModel>>
    val recommendedConnectionsChannel: BroadcastChannel<List<ShortAccountModel>>
    val allConnectionsChannel: BroadcastChannel<List<ShortAccountModel>>

    fun connect(account: ShortAccountModel)
    fun disconnect(account: ShortAccountModel)
    fun accept(account: ShortAccountModel)
    fun decline(account: ShortAccountModel)

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun onContactPermissionsGranted()
}