package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.impl.ChatMessageModelImpl
import com.mnassa.domain.repository.ChatRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
class ChatInteractorImpl(private val chatRepository: ChatRepository, private val userRepository: UserRepository) : ChatInteractor {

    override suspend fun listOfChats(): ReceiveChannel<ListItemEvent<ChatRoomModel>> =
            chatRepository.listOfChats()

    override suspend fun listOfChatsImmediately(): List<ChatRoomModel> = chatRepository.listOfChatsImmediately()

    override suspend fun listOfMessages(chatId: String, accountId: String?): ReceiveChannel<ListItemEvent<ChatMessageModel>> =
            chatRepository.listOfMessages(chatId, accountId)

    override suspend fun sendMessage(chatID: String, text: String, type: String, linkedMessageId: String?, linkedPostId: String?) {
        val postPair = linkedPostId?.let { Pair(it, null) }?:kotlin.run { null }
        val chatPair = linkedMessageId?.let { Pair(it, null) }?:kotlin.run { null }
        chatRepository.sendMessage(ChatMessageModelImpl(
                createdAt = Date(),
                creator = requireNotNull(userRepository.getAccountIdOrException()),
                text = text,
                type = type,
                chatID = chatID,
                replyMessage = chatPair,
                replyPost = postPair,
                id = ""
        ))
    }

    override suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean) {
        chatRepository.deleteMessage(messageId, chatID, isDeleteForBoth)
    }

    override suspend fun getChatIdByUserId(accountId: String?): String =
            accountId?.run { chatRepository.getChatIdByUserId(this) } ?: chatRepository.getSupportChat()

    override suspend fun resetChatUnreadCount(chatId: String) {
        chatRepository.resetChatUnreadCount(chatId)
    }
}