package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.network.bean.firebase.ChatDbModel
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_LIST
import com.mnassa.data.repository.DatabaseContract.TABLE_CHAT_TYPE
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.repository.ChatRepository
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
                         private val onverter: ConvertersContext
) : ChatRepository {

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
                    it.item.lastMessageModel?.account = userRepository
                            .getById(it.item.members?.first { it != userId }
                            ?: "")
                    it
                }
                .filter {
                    it.item.lastMessageModel?.account != null
                }
    }
}