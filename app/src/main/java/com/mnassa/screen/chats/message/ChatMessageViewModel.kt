package com.mnassa.screen.chats.message

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

interface ChatMessageViewModel : MnassaViewModel {
    val messageChannel: BroadcastChannel<ListItemEvent<ChatMessageModel>>
    val accountChannel: BroadcastChannel<String>

    suspend fun retrieveMyAccount() :String
    fun retrieveChatId(accointId: String)
    fun resetChatUnreadCount()
    fun sendMessage(text: String, type: String, linkedMessageId: String?, linkedPostId: String?)
    fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean)
    fun retrieveChatWithAdmin()

}