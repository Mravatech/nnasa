package com.mnassa.screen.buildnetwork

import android.Manifest
import androidx.annotation.RequiresPermission
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface BuildNetworkViewModel : MnassaViewModel {
    val usersToInviteChannel: BroadcastChannel<List<ShortAccountModel>>
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun onContactPermissionsGranted()

    fun inviteUsers(accountIds: List<String>)

    sealed class OpenScreenCommand {
        class MainScreen : OpenScreenCommand()
    }
}