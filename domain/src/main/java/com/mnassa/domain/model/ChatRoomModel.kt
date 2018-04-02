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
}


interface ChatMessageModel {
    val createdAt: Date
    val creator: String
    val text: String
    val type: String
    var account: ShortAccountModel?
}