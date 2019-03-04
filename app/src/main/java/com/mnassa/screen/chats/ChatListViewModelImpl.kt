package com.mnassa.screen.chats

import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListViewModelImpl(private val chatInteractor: ChatInteractor) : MnassaViewModelImpl(), ChatListViewModel {

    override val listMessagesChannel: BroadcastChannel<ListItemEvent<List<ChatRoomModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = { it.send(ListItemEvent.Cleared()) },
            receiveChannelProvider = { chatInteractor.loadChatListWithChangesHandling() })
}