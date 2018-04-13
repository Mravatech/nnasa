package com.mnassa.domain.model

import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
interface NotificationModel {
    val createdAt: Date
    val text: String
    val type: String
    val extra: NotificationExtra
    var isOld: Boolean
}

interface NotificationExtra : ShortAccountModel {

}
