package com.mnassa.data.repository

import com.google.firebase.database.DatabaseReference
import com.mnassa.core.converter.ConvertersContext
import com.mnassa.data.extensions.DEFAULT_LIMIT
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.network.api.FirebaseChatApi
import com.mnassa.data.network.bean.firebase.ChatDbModel
import com.mnassa.data.network.bean.firebase.ChatMessageDbModel
import com.mnassa.data.network.bean.retrofit.request.ChatRoomRequest
import com.mnassa.data.network.bean.retrofit.request.ChatUnreadCountRequest
import com.mnassa.data.network.bean.retrofit.request.MessageFromChatRequest
import com.mnassa.data.network.bean.retrofit.request.MessageRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_LIST
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_MESSAGES
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_TYPE_PRIVATE
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.repository.ChatRepository
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel


class ChatRepositoryImpl(private val db: DatabaseReference,
                         private val userRepository: UserRepository,
                         private val exceptionHandler: ExceptionHandler,
                         private val converter: ConvertersContext,
                         private val postsRepository: PostsRepository,
                         private val chatApi: FirebaseChatApi
) : ChatRepository {

    override suspend fun getChatIdByUserId(accountId: String): String =
            chatApi.addChat(ChatRoomRequest(accountId)).handleException(exceptionHandler).data.chatID

    override suspend fun getSupportChat(): String {
        return chatApi.addSupprotyChat().await().data.chatID
    }

    override suspend fun resetChatUnreadMessagesCount(chatId: String) {
        chatApi.resetChatUnreadCount(ChatUnreadCountRequest(chatId)).handleException(exceptionHandler)
    }

    override suspend fun sendMessage(message: ChatMessageModel) {
        chatApi.sendMessage(MessageRequest(
                type = message.type,
                text = message.text,
                chatID = requireNotNull(message.chatID),
                linkedMessageId = message.replyMessage?.first,
                linkedPostId = message.replyPost?.first
        )).handleException(exceptionHandler)
    }

    override suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean) {
        chatApi.deleteMessage(MessageFromChatRequest(messageId, chatID, isDeleteForBoth)).handleException(exceptionHandler)
    }

    override suspend fun loadMessagesWithChangesHandling(chatId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>> {
        val myUserId = userRepository.getAccountIdOrException()
        return db.child(TABLE_CHAT)
                .child(TABLE_CHAT_MESSAGES)
                .child(TABLE_CHAT_TYPE_PRIVATE)
                .child(myUserId)
                .child(chatId)
                .toValueChannelWithChangesHandling<ChatMessageDbModel, ChatMessageModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapChatMessage(converter.convert(it, ChatMessageModel::class.java), chatId) }
                )
    }

    override suspend fun preloadMessages(chatId: String): List<ChatMessageModel> {
        val myUserId = userRepository.getAccountIdOrException()
        return db.child(TABLE_CHAT)
                .child(TABLE_CHAT_MESSAGES)
                .child(TABLE_CHAT_TYPE_PRIVATE)
                .child(myUserId)
                .child(chatId)
                .limitToLast(DEFAULT_LIMIT)
                .awaitList<ChatMessageDbModel>(exceptionHandler)
                .let { converter.convertCollection(it, ChatMessageModel::class.java) }
                .map { mapChatMessage(it, chatId) }
    }


    override suspend fun loadChatListWithChangesHandling(): ReceiveChannel<ListItemEvent<ChatRoomModel>> {
        val userId = userRepository.getAccountIdOrException()
        return db.child(TABLE_CHAT)
                .child(TABLE_CHAT_LIST)
                .child(TABLE_CHAT_TYPE_PRIVATE)
                .child(userId)
                .toValueChannelWithChangesHandling<ChatDbModel, ChatRoomModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapChatModel(it, userId) }
                )
    }

    override suspend fun preloadChatList(): List<ChatRoomModel> {
        val userId = userRepository.getAccountIdOrException()
        return db.child(TABLE_CHAT)
                .child(TABLE_CHAT_LIST)
                .child(TABLE_CHAT_TYPE_PRIVATE)
                .child(userId)
                .awaitList<ChatDbModel>(exceptionHandler)
                .mapNotNull { mapChatModel(it, userId) }
    }

    private suspend fun mapChatModel(chat: ChatDbModel, userId: String): ChatRoomModel? {
        val chat = converter.convert(chat, ChatRoomModel::class.java)
        val otherUserId = chat.members?.firstOrNull { it != userId }
        if (otherUserId != null) {
            chat.account = userRepository.getAccountById(otherUserId)
        }
        return chat.takeIf { it.account != null }
    }

    private suspend fun mapChatMessage(post: ChatMessageModel, chatId: String): ChatMessageModel {
        val myUserId = userRepository.getAccountIdOrException()
        post.replyMessage?.first?.let { first ->
            val replyMessage: ChatMessageDbModel? = getReplyMessage(myUserId, chatId, first)
            replyMessage?.let { _ ->
                post.replyMessage = post.replyMessage?.copy(second = converter.convert(replyMessage, ChatMessageModel::class.java))
            }
        }
        post.replyPost?.first?.let { first ->
            val replyPost: PostModel? = postsRepository.loadById(first).receiveOrNull()
            replyPost?.let { post1 ->
                post.replyPost = post.replyPost?.copy(second = post1)
            }
        }
        return post
    }

    private suspend fun getReplyMessage(myUserId: String, chatId: String, first: String): ChatMessageDbModel? =
            db.child(TABLE_CHAT)
                    .child(TABLE_CHAT_MESSAGES)
                    .child(TABLE_CHAT_TYPE_PRIVATE)
                    .child(myUserId)
                    .child(chatId)
                    .child(first)
                    .await(exceptionHandler)
}
