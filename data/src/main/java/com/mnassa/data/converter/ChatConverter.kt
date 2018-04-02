package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.ChatDbModel
import com.mnassa.data.network.bean.firebase.ChatLastMessageDbModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.LastMessageModel
import com.mnassa.domain.model.impl.ChatRoomModelImpl
import com.mnassa.domain.model.impl.LastMessageModelImpl

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::chatListConvert)
    }

    private fun chatListConvert(input: ChatDbModel, token: Any?, converter: ConvertersContext): ChatRoomModel {
        return ChatRoomModelImpl(
                viewedAtDate = input.viewedAtDate,
                viewedAt = input.viewedAt,
                unreadCount = input.unreadCount,
                lastMessageModel = input.lastMessage?.let { lastChatConvert(it) },
                id = input.id,
                members =  input.members?.keys?.toList()
        )
    }

    private fun lastChatConvert(input: ChatLastMessageDbModel): LastMessageModel {
        return LastMessageModelImpl(
                createdAt = input.createdAt,
                createdAtDate = input.createdAtDate,
                creator = input.creator,
                text = input.text,
                type = input.type,
                account = null
        )
    }

}