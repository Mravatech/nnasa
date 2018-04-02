package com.mnassa.screen.chats

import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListViewModelImpl : MnassaViewModelImpl(), ChatListViewModel {
    override val messagesChannel: BroadcastChannel<List<Any>> = BroadcastChannel(10)


}