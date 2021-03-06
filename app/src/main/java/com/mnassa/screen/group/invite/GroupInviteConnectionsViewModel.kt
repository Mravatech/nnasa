package com.mnassa.screen.group.invite

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 5/23/2018.
 */
interface GroupInviteConnectionsViewModel : MnassaViewModel {
    val connectionsChannel: BroadcastChannel<List<UserInvite>>
    val closeScreenChannel: BroadcastChannel<Unit>

    fun sendInvite(user: ShortAccountModel)
    fun revokeInvite(user: ShortAccountModel)
    fun removeUser(user: ShortAccountModel)
}