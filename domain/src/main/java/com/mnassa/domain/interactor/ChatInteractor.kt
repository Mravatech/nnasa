package com.mnassa.domain.interactor

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
interface ChatInteractor {

    suspend fun listOfChats(): ReceiveChannel<ListItemEvent<ChatRoomModel>>
    suspend fun listOfMessages(chatId: String, accointId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>>
    suspend fun getChatIdByUserId(accountId: String): String
    suspend fun sendMessage(chatID: String, text: String, type: String, linkedMessageId: String?, linkedPostId: String?)
    suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean)
    suspend fun resetChatUnreadCount(chatId: String)
}