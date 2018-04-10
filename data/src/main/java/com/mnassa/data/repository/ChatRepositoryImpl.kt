package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.network.api.FirebaseChatApi
import com.mnassa.data.network.bean.firebase.ChatDbModel
import com.mnassa.data.network.bean.firebase.ChatMessageDbModel
import com.mnassa.data.network.bean.retrofit.request.ChatRoomRequest
import com.mnassa.data.network.bean.retrofit.request.ChatUnreadCountRequest
import com.mnassa.data.network.bean.retrofit.request.MessageFromChatRequest
import com.mnassa.data.network.bean.retrofit.request.MessageRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_LIST
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_MESSAGES
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_TYPE
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.repository.ChatRepository
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.filter
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatRepositoryImpl(private val db: DatabaseReference,
                         private val userRepository: UserRepository,
                         private val exceptionHandler: ExceptionHandler,
                         private val converter: ConvertersContext,
                         private val postsRepository: PostsRepository,
                         private val chatApi: FirebaseChatApi
) : ChatRepository {

    override suspend fun getChatIdByUserId(accountId: String): String =
            chatApi.addChat(ChatRoomRequest(accountId)).await().data.chatID

    override suspend fun resetChatUnreadCount(chatId: String) {
        chatApi.resetChatUnreadCount(ChatUnreadCountRequest(chatId))
    }

    override suspend fun sendMessage(message: ChatMessageModel) {
        chatApi.sendMessage(MessageRequest(
                type = message.type,
                text = message.text,
                chatID = requireNotNull(message.chatID),
                linkedMessageId = message.replyMessage?.first,
                linkedPostId = message.replyPost?.first
        ))
    }

    override suspend fun deleteMessage(messageId: String, chatID: String, isDeleteForBoth: Boolean) {
        chatApi.deleteMessage(MessageFromChatRequest(messageId, chatID, isDeleteForBoth))
    }

    override suspend fun listOfMessages(chatId: String, accointId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>> {
        val myUserId = requireNotNull(userRepository.getAccountId())
        return db.child(TABLE_CHAT)
                .child(TABLE_CHAT_MESSAGES)
                .child(TABLE_CHAT_TYPE)
                .child(myUserId)
                .child(chatId)
                .apply { keepSynced(true) }
                .toValueChannelWithChangesHandling<ChatMessageDbModel, ChatMessageModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = converter.convertFunc(ChatMessageModel::class.java)
                )
                .map {
                    it.item.replyMessage?.first?.let { first ->
                        val replyMessage: ChatMessageDbModel? = db.child(TABLE_CHAT)
                                .child(TABLE_CHAT_MESSAGES)
                                .child(TABLE_CHAT_TYPE)
                                .child(myUserId)
                                .child(chatId)
                                .child(first)
                                .await(exceptionHandler)
                        replyMessage?.let { _ ->
                            it.item.replyMessage = it.item.replyMessage?.copy(second = converter.convert(replyMessage, ChatMessageModel::class.java))
                        }
                    }
                    it.item.replyPost?.first?.let { first ->
                        val replyPost: Post? = postsRepository.loadUserPostById(accointId, first)
                        replyPost?.let { _ ->
                            it.item.replyPost = it.item.replyPost?.copy(second = converter.convert(replyPost, Post::class.java))
                        }
                    }
                    it
                }
    }

    override suspend fun listOfChats(): ReceiveChannel<ListItemEvent<ChatRoomModel>> {
        val userId = requireNotNull(userRepository.getAccountId())
        return db.child(TABLE_CHAT)
                .child(TABLE_CHAT_LIST)
                .child(TABLE_CHAT_TYPE)
                .child(userId)
                .apply { keepSynced(true) }
                .toValueChannelWithChangesHandling<ChatDbModel, ChatRoomModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = converter.convertFunc(ChatRoomModel::class.java)
                )
                .map {
                    it.item.account = userRepository
                            .getById(it.item.members?.first { it != userId }
                                    ?: "")
                    it
                }
                .filter {
                    it.item.account != null
                }
    }
}
