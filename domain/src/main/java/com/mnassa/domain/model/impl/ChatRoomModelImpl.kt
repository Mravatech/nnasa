package com.mnassa.domain.model.impl

import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ShortAccountModel
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
data class ChatRoomModelImpl(
        override val viewedAtDate: Date,
        override val unreadCount: Int,
        override val chatMessageModel: ChatMessageModel?,
        override var id: String,
        override var members: List<String>?
        ) : ChatRoomModel

data class ChatMessageModelImpl(
        override val createdAt: Date,
        override val creator: String,
        override val text: String,
        override val type: String,
        override var account: ShortAccountModel?
) : ChatMessageModel
