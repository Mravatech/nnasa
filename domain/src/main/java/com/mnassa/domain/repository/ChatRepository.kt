package com.mnassa.domain.repository

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.channels.ReceiveChannel

interface ChatRepository {
    suspend fun loadChatListWithChangesHandling(): ReceiveChannel<ListItemEvent<ChatRoomModel>>
    suspend fun preloadChatList(): List<ChatRoomModel>

    suspend fun loadMessagesWithChangesHandling(chatId: String, accountId: String?): ReceiveChannel<ListItemEvent<ChatMessageModel>>
    suspend fun preloadMessages(chatId: String, accountId: String?): List<ChatMessageModel>

    suspend fun getChatIdByUserId(accountId: String): String
    suspend fun getSupportChat(): String
    suspend fun sendMessage(message: ChatMessageModel)
    suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean)
    suspend fun resetChatUnreadMessagesCount(chatId: String)
}