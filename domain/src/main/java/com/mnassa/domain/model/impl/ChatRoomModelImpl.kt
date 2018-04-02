package com.mnassa.domain.model.impl

import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.LastMessageModel
import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
data class ChatRoomModelImpl(
        override val viewedAtDate: Long,
        override val viewedAt: String,
        override val unreadCount: Int,
        override val lastMessageModel: LastMessageModel?,
        override var id: String,
        override var members: List<String>?
        ) : ChatRoomModel

data class LastMessageModelImpl(
        override val createdAt: Long,
        override val createdAtDate: String,
        override val creator: String,
        override val text: String,
        override val type: String,
        override var account: ShortAccountModel?
) : LastMessageModel
