package com.mnassa.domain.model.impl

import com.mnassa.domain.model.ChatModel
import com.mnassa.domain.model.LastMessageModel
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
data class ChatModelImpl(
        override val viewedAtDate: String,
        override val viewedAt: Date,
        override val unreadCount: Int,
        override val lastMessageModel: LastMessageModel
) : ChatModel

data class LastMessageModelImpl(
        override val createdAt: Date,
        override val createdAtDate: String,
        override val unreadCount: Int,
        override val creator: String,
        override val text: String,
        override val type: String
) : LastMessageModel