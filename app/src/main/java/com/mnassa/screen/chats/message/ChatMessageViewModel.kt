package com.mnassa.screen.chats.message

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
interface ChatMessageViewModel : MnassaViewModel {
    suspend fun getMessageChannel(chatId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>>
}