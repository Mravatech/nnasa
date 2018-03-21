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
}

interface DeclinedShortAccountModel : ShortAccountModel {
    var declinedAt: Date
}

interface PersonalAccountDiffModel : Serializable {
    var firstName: String
    var lastName: String
}

interface OrganizationAccountDiffModel : Serializable {
    var organizationName: String
}

enum class AccountType {
    PERSONAL, ORGANIZATION
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
