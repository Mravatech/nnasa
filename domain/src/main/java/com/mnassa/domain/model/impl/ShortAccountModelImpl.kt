package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by Peter on 2/28/2018.
 */
data class ShortAccountModelImpl(override var id: String,
                                 override var serialNumber: Int?,
                                 override var userName: String,
                                 override var accountType: AccountType,
                                 override var avatar: String?,
                                 override var contactPhone: String?,
                                 override var language: String?,
                                 override var personalInfo: PersonalAccountDiffModel?,
                                 override var organizationInfo: OrganizationAccountDiffModel?,
                                 override var abilities: List<AccountAbility>,
                                 override var connectedBy: ConnectedByModel?) : ShortAccountModel

data class DeclinedShortAccountModelImpl(
        override var id: String,
        override var serialNumber: Int?,
        override var userName: String,
        override var accountType: AccountType,
        override var avatar: String?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>,
        override var declinedAt: Date,
        override var connectedBy: ConnectedByModel?
) : DeclinedShortAccountModel

data class PersonalInfoModelImpl(
        override var id: String,
        override var serialNumber: Int?,
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
        override val gender: Gender,
        override var connectedBy: ConnectedByModel?
) : PersonalInfoModel

data class ProfilePersonalInfoModelImpl(
        override var id: String,
        override var serialNumber: Int?,
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
        override val gender: Gender,
        override val locationId: String?,
        override val interests: List<String>,
        override val offers: List<String>,
        override var connectedBy: ConnectedByModel?
) : ProfilePersonalInfoModel

data class CompanyInfoModelImpl(
        override var id: String,
        override var serialNumber: Int?,
        override var userName: String,
        override var accountType: AccountType,
        override var avatar: String?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>,
        override val showContactEmail: Boolean?,
        override val showContactPhone: Boolean?,
        override val contactEmail: String?,
        override val founded: Long?,
        override val organizationType: String?,
        override val website: String?,
        override val foundedDate: String?,
        override var connectedBy: ConnectedByModel?
) : CompanyInfoModel

data class ProfileCompanyInfoModelImpl(
        override var id: String,
        override var serialNumber: Int?,
        override var userName: String,
        override var accountType: AccountType,
        override var avatar: String?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>,
        override val showContactEmail: Boolean?,
        override val showContactPhone: Boolean?,
        override val contactEmail: String?,
        override val founded: Long?,
        override val organizationType: String?,
        override val website: String?,
        override val foundedDate: String?,
        override val locationId: String?,
        override val interests: List<String>,
        override val offers: List<String>,
        override var connectedBy: ConnectedByModel?
) : ProfileCompanyInfoModel

data class PersonalAccountDiffModelImpl(override var firstName: String, override var lastName: String) : PersonalAccountDiffModel

data class OrganizationAccountDiffModelImpl(override var organizationName: String) : OrganizationAccountDiffModel