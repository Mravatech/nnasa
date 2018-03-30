package com.mnassa.screen.profile

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
interface ProfileViewModel : MnassaViewModel {
    val profileChannel: BroadcastChannel<ProfileModel>
    val profileClickChannel: BroadcastChannel<ProfileCommand>
    val statusesConnectionsChannel: BroadcastChannel<String?>
    suspend fun getPostsById(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    fun getProfileWithAccountId(accountId: String)
    fun connectionClick()
    fun walletClick()
    fun <T> handleException(function: suspend () -> T): Job

    sealed class ProfileCommand {
        class ProfileConnection: ProfileCommand()
        class ProfileWallet: ProfileCommand()
    }

}