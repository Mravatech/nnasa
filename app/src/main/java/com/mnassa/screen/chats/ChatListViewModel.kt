package com.mnassa.screen.chats

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface ChatListViewModel : MnassaViewModel {

    val messagesChannel: BroadcastChannel<List<Any>>

}