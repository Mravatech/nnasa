package com.mnassa.domain.model

import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

interface ChatRoomModel : Model {
    val viewedAtDate: Date
    val unreadCount: Int
    val chatMessageModel: ChatMessageModel?
    var members: List<String>?
    var account: ShortAccountModel?

    companion object {
        const val DEFAULT_VIEWED_AT_DATE = 0L
        const val DEFAULT_UNREAD_COUNT = 0
    }
}


interface ChatMessageModel : Model {
    val createdAt: Date
    val creator: String
    val text: String
    val type: String
    val chatID: String?
    var replyMessage: Pair<String, ChatMessageModel?>?
    var replyPost: Pair<String, PostModel?>?

    companion object {
        const val DEFAULT_CREATED_AT_DATE = 0L
        const val DEFAULT_CREATOR = HasIdMaybe.EMPTY_KEY
        const val DEFAULT_TYPE = "text"
    }
}