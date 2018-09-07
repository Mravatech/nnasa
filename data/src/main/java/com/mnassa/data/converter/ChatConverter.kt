package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.ChatDbModel
import com.mnassa.data.network.bean.firebase.ChatMessageDbModel
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.impl.ChatMessageModelImpl
import com.mnassa.domain.model.impl.ChatRoomModelImpl
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::chatListConvert)
        convertersContext.registerConverter(this::messageChatConvert)
    }

    private fun chatListConvert(input: ChatDbModel, token: Any?, converter: ConvertersContext): ChatRoomModel {
        return ChatRoomModelImpl(
                viewedAtDate = Date(input.viewedAtDate),
                unreadCount = input.unreadCount,
                chatMessageModel = input.lastMessage?.let { converter.convert(it, ChatMessageModel::class.java) },
                id = input.id,
                members = input.members?.keys?.toList(),
                account = null
        )
    }

    private fun messageChatConvert(input: ChatMessageDbModel, token: Any?, converter: ConvertersContext): ChatMessageModel {
        val postPair = input.linkedPostId?.let { Pair(it, null) }?:kotlin.run { null }
        val chatPair = input.linkedMessageId?.let { Pair(it, null) }?:kotlin.run { null }
        return ChatMessageModelImpl(
                createdAt = Date(input.createdAt),
                creator = input.creator,
                text = input.text,
                type = input.type,
                chatID = input.chatID,
                replyMessage = chatPair,
                replyPost = postPair,
                id = input.id ?: ""
        )
    }

}