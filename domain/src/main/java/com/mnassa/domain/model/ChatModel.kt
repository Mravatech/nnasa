package com.mnassa.domain.model

import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

interface ChatModel {

    val viewedAtDate: String
    val viewedAt: Date
    val unreadCount: Int
    val lastMessageModel: LastMessageModel
//    val members: String
}


interface LastMessageModel {
    val unreadCount: Int
    val createdAt: Date
    val createdAtDate: String
    val creator: String
    val text: String
    val type: String
}