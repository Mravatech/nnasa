package com.mnassa.domain.interactor

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.channels.ReceiveChannel

interface ChatInteractor {

    suspend fun loadMessagesWithChangesHandling(chatId: String): ReceiveChannel<ListItemEvent<List<ChatMessageModel>>>
    suspend fun loadChatListWithChangesHandling(): ReceiveChannel<ListItemEvent<List<ChatRoomModel>>>
    suspend fun getChatIdByUserId(accountId: String?): String
    suspend fun sendMessage(chatID: String, text: String, type: String, linkedMessageId: String?, linkedPostId: String?)
    suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean)
    suspend fun resetChatUnreadCount(chatId: String)
}