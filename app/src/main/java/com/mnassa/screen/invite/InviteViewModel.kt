package com.mnassa.screen.invite

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.login.enterphone.EnterPhoneViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface InviteViewModel : MnassaViewModel {
    val usersToInviteChannel: BroadcastChannel<List<ShortAccountModel>>
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun onContactPermissionsGranted()

    fun inviteUsers(accountIds: List<String>)

    sealed class OpenScreenCommand {
        class MainScreen: OpenScreenCommand()
    }
}