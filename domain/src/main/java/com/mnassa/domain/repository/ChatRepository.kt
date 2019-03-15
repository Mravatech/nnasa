package com.mnassa.domain.repository

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.channels.ReceiveChannel

interface ChatRepository {
    suspend fun loadChatListWithChangesHandling(): ReceiveChannel<ListItemEvent<ChatRoomModel>>

    suspend fun loadMessagesWithChangesHandling(chatId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>>

    suspend fun getChatIdByUserId(accountId: String): String
    suspend fun getSupportChat(): String
    suspend fun sendMessage(message: ChatMessageModel)
    suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean)
    suspend fun resetChatUnreadMessagesCount(chatId: String)
}