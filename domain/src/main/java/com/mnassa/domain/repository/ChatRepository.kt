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
    suspend fun listOfChatsImmediately(): List<ChatRoomModel>
    suspend fun listOfMessages(chatId: String, accountId: String?): ReceiveChannel<ListItemEvent<ChatMessageModel>>
    suspend fun getChatIdByUserId(accountId: String): String
    suspend fun getSupportChat(): String
    suspend fun sendMessage(message: ChatMessageModel)
    suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean)
    suspend fun resetChatUnreadCount(chatId: String)
}