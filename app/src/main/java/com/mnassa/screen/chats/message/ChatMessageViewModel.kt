package com.mnassa.screen.chats.message

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

interface ChatMessageViewModel : MnassaViewModel {
    val messageChannel: BroadcastChannel<ListItemEvent<List<ChatMessageModel>>>
    val currentUserAccountId: String

    suspend fun sendMessage(text: String, type: String, linkedMessage: ChatMessageModel?, linkedPost: PostModel?): Boolean
    fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean)

    data class Params(
            val accountId: String?,
            val chatId: String?
    )
}