package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
interface NotificationModel : Model {
    val createdAt: Date
    val text: String
    val type: String
    val extra: NotificationExtra
    var isOld: Boolean
}

interface NotificationExtra : Serializable {
    val author: ShortAccountModel?
    val post: PostModel?
    var reffered: ShortAccountModel?
    var recommended: ShortAccountModel?
    var group: GroupModel?
    val eventName: String?
    val ticketsPrice: String?
    val totalPrice: String?
    val attendee: String?
    val event: EventModel?
    val newInviteNumber: Int?
}