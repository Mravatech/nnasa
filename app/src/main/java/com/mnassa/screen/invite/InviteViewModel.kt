package com.mnassa.screen.invite

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface InviteViewModel : MnassaViewModel {
    val usersToInviteChannel: BroadcastChannel<List<ShortAccountModel>>

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun onContactPermissionsGranted()

    fun inviteUsers(accountIds: List<String>)
}