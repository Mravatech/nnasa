package com.mnassa.screen.profile

import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.TranslatedWordModel
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
    val statusesConnectionsChannel: BroadcastChannel<ConnectionStatus>
    val postChannel: BroadcastChannel<ListItemEvent<PostModel>>

    fun getPostsById(accountId: String)
    fun getProfileWithAccountId(accountId: String)
    fun sendConnectionStatus(connectionStatus: ConnectionStatus, aid: String, isAcceptConnect: Boolean)

    fun sendComplaint(id: String, reason: String)
    suspend fun retrieveComplaints():List<TranslatedWordModel>
    sealed class ProfileCommand {
        class ProfileConnection : ProfileCommand()
        class ProfileWallet : ProfileCommand()
    }

}