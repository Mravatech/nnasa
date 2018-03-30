package com.mnassa.screen.profile

import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
interface ProfileViewModel : MnassaViewModel {
    val profileChannel: BroadcastChannel<ProfileModel>
    val profileClickChannel: BroadcastChannel<ProfileCommand>
    val statusesConnectionsChannel: BroadcastChannel<String?>
    fun getProfileWithAccountId(accountId: String)
    fun connectionClick()
    fun walletClick()

    sealed class ProfileCommand {
        class ProfileConnection: ProfileCommand()
        class ProfileWallet: ProfileCommand()
    }

}