package com.mnassa.domain.model

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

interface ChatRoomModel : Model {

    val viewedAtDate: Long
    val viewedAt: String
    val unreadCount: Int
    val lastMessageModel: LastMessageModel?
    var members: List<String>?
}


interface LastMessageModel {
    val createdAt: Long
    val createdAtDate: String
    val creator: String
    val text: String
    val type: String
    var account: ShortAccountModel?
}