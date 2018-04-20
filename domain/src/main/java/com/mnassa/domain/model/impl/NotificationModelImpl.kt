package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */

data class NotificationModelImpl(
        override var id: String,
        override val createdAt: Date,
        override val text: String,
        override val type: String,
        override val extra: NotificationExtra,
        override var isOld: Boolean
) : NotificationModel

data class NotificationExtraImpl(
        override val author: ShortAccountModel?,
        override val post: PostModel?,
        override var reffered: ShortAccountModel?,
        override var recommended: ShortAccountModel?,
        override val eventName: String?,
        override val ticketsPrice: String?,
        override val totalPrice: String?,
        override val attendee: String?,
        override val event: EventModel?
) : NotificationExtra