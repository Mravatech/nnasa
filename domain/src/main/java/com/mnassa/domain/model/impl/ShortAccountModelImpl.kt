package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by Peter on 2/28/2018.
 */
data class ShortAccountModelImpl(override var id: String,
                                 override var firebaseUserId: String,
                                 override var userName: String,
                                 override var accountType: AccountType,
                                 override var avatar: String?,
                                 override var contactPhone: String?,
                                 override var language: String?,
                                 override var personalInfo: PersonalAccountDiffModel?,
                                 override var organizationInfo: OrganizationAccountDiffModel?,
                                 override var abilities: List<AccountAbility>) : ShortAccountModel {
}

data class DeclinedShortAccountModelImpl(
        override var id: String,
        override var firebaseUserId: String,
        override var userName: String,
        override var accountType: AccountType,
        override var avatar: String?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>,
        override var declinedAt: Date
) : DeclinedShortAccountModel

data class PersonalInfoModelImpl(
        override var id: String,
        override var firebaseUserId: String,
        override var userName: String,
        override var accountType: AccountType,
        override var avatar: String?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>,
        override val birthdayDate: String?,
        override val showContactEmail: Boolean?,
        override val birthday: Long?,
        override val showContactPhone: Boolean?,
        override val contactEmail: String?,
        override val gender: Gender
) : PersonalInfoModel

data class PersonalAccountDiffModelImpl(override var firstName: String, override var lastName: String) : PersonalAccountDiffModel

data class OrganizationAccountDiffModelImpl(override var organizationName: String) : OrganizationAccountDiffModel