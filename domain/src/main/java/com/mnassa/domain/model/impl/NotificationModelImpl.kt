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
        override var author: ShortAccountModel?,
        override val post: NotificationExtra.Post?,
        override var reffered: NotificationExtra.UserReferred?,
        override var recommended: NotificationExtra.UserRecommended?,
        override var group: GroupModel?,
        override val eventName: String?,
        override val ticketsPrice: String?,
        override val totalPrice: String?,
        override val attendee: String?,
        override val event: NotificationExtra.Event?,
        override val newInviteNumber: Int?
) : NotificationExtra

data class NotificationExtraUserReferredImpl(
        override var id: String,
        override var userName: String,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?
) : NotificationExtra.UserReferred

data class NotificationExtraUserRecommendedImpl(
        override var id: String,
        override var userName: String,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?
) : NotificationExtra.UserRecommended

data class NotificationExtraPostImpl(
        override var id: String,
        override var author: ShortAccountModel?,
        override var type: String,
        override var text: String
) : NotificationExtra.Post

data class NotificationExtraEventImpl(
        override var id: String,
        override var author: ShortAccountModel?,
        override var title: String
) : NotificationExtra.Event
