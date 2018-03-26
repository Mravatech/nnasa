package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */
data class ProfileAccountModelImpl(
        override val createdAt: Long?,
        override var id: String,
        override val createdAtDate: String?,
        override var firebaseUserId: String,
        override val interests: List<String>?,
        override val offers: List<String>?,
        override var userName: String,
        override val points: Int?,
        override var accountType: AccountType,
        override val totalIncome: Int?,
        override var avatar: String?,
        override val totalOutcome: Int?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>) : ProfileAccountModel