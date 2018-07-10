package com.mnassa.screen.chats

import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface ChatListViewModel : MnassaViewModel {

    val listMessagesChannel: BroadcastChannel<ListItemEvent<List<ChatRoomModel>>>
}