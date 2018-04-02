package com.mnassa.domain.repository

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

interface ChatRepository {
    suspend fun listOfChats(): ReceiveChannel<ListItemEvent<ChatRoomModel>>
    suspend fun listOfMessages(chatId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>>
}