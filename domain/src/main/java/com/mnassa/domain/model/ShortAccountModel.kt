package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
interface ShortAccountModel : Model {
    override var id: String // account ID
    var firebaseUserId: String // fireBase user ID
    var userName: String
    var accountType: AccountType
    //
    var avatar: String?
    var contactPhone: String?
    var language: String?
    //
    var personalInfo: PersonalAccountDiffModel?
    var organizationInfo: OrganizationAccountDiffModel?
    //
    var abilities: List<AccountAbility>
    //
    var connectedBy: ConnectedByModel?
}

interface DeclinedShortAccountModel : ShortAccountModel {
    var declinedAt: Date
}

interface PersonalInfoModel : ShortAccountModel {
    val birthdayDate: String?
    val showContactEmail: Boolean?
    val birthday: Long?
    val showContactPhone: Boolean?
    val contactEmail: String?
    val gender: Gender
}


interface ProfilePersonalInfoModel : PersonalInfoModel {
    val locationId: String?
    val interests: List<String>
    val offers: List<String>
}

interface CompanyInfoModel : ShortAccountModel {
    val showContactEmail: Boolean?
    val showContactPhone: Boolean?
    val contactEmail: String?
    val founded: Long?
    val foundedDate: String?
    val organizationType: String?
    val website: String?
}

interface ProfileCompanyInfoModel : CompanyInfoModel {
    val locationId: String?
    val interests: List<String>
    val offers: List<String>
}

interface PersonalAccountDiffModel : Serializable {
    var firstName: String
    var lastName: String
}

interface OrganizationAccountDiffModel : Serializable {
    var organizationName: String
}

interface ConnectedByModel : Serializable {
    val id: String?
    val type: String
    val value: String
}

enum class AccountType {
    PERSONAL, ORGANIZATION
}

enum class Gender {
    MALE, FEMALE
}

val ShortAccountModel.formattedName: String
    get () {
        return when (accountType) {
            AccountType.PERSONAL -> {
                val info = requireNotNull(personalInfo)
                info.firstName + " " + info.lastName
            }
            AccountType.ORGANIZATION -> {
                requireNotNull(organizationInfo?.organizationName)
            }
        }
    }

fun ShortAccountModel.mainAbility(atPlaceholder: String): String? {
    val ability = abilities.firstOrNull { it.isMain } ?: abilities.firstOrNull() ?: return null
    return when {
        ability.name.isNullOrBlank() && ability.place.isNullOrBlank() -> "${ability.name} $atPlaceholder ${ability.place}"
        else -> ability.name ?: ability.place
    }
}