package com.mnassa.screen.chats.message

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

interface ChatMessageViewModel : MnassaViewModel {
    val messageChannel: BroadcastChannel<ListItemEvent<ChatMessageModel>>
    val currentUserAccountId: String

    fun resetChatUnreadCount()
    fun sendMessage(text: String, type: String, linkedMessage: ChatMessageModel?, linkedPost: PostModel?)
    fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean)
}