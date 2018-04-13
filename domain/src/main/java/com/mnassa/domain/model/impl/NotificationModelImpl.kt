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
        override val extra: NotificationExtra?,
        override var isOld: Boolean
) : NotificationModel

data class NotificationExtraImpl(
        override var id: String,
        override var firebaseUserId: String,
        override var userName: String,
        override var accountType: AccountType,
        override var avatar: String?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>
) : NotificationExtra