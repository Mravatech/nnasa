package com.mnassa.screen.chats.startchat

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
interface ChatConnectionsViewModel : MnassaViewModel {
    val allConnectionsChannel: BroadcastChannel<List<ShortAccountModel>>
}