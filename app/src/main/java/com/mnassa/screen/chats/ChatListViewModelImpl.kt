package com.mnassa.screen.chats

import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListViewModelImpl(private val chatInteractor: ChatInteractor) : MnassaViewModelImpl(), ChatListViewModel {

    override val listMessagesChannel: BroadcastChannel<ListItemEvent<List<ChatRoomModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            invokeReConsumeFirstly = true,
            beforeReConsume = {
                it.send(ListItemEvent.Cleared())
                it.send(ListItemEvent.Added(chatInteractor.listOfChatsImmediately()))
            },
            receiveChannelProvider = {
                chatInteractor.listOfChats().map { it.toBatched() }
            })

}