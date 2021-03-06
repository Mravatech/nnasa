package com.mnassa.screen.connections.recommended

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 09.03.2018.
 */
interface RecommendedConnectionsViewModel : MnassaViewModel {
    val recommendedConnectionsChannel: BroadcastChannel<RecommendedConnections>

    fun connect(accountModel: ShortAccountModel)
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun onContactPermissionsGranted()
}