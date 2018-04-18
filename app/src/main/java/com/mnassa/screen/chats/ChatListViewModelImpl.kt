package com.mnassa.screen.chats

import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.extensions.ReConsumeWhenAccountChangedArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListViewModelImpl(private val chatInteractor: ChatInteractor) : MnassaViewModelImpl(), ChatListViewModel {
//    override suspend fun getMessagesChannel(): ReceiveChannel<ListItemEvent<ChatRoomModel>> = chatInteractor.listOfChats()


override val listMessagesChannel: BroadcastChannel<ListItemEvent<ChatRoomModel>> by ReConsumeWhenAccountChangedArrayBroadcastChannel(
        beforeReConsume = { it.send(ListItemEvent.Cleared()) },
        receiveChannelProvider = { chatInteractor.listOfChats() })

}