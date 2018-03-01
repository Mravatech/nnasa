package com.mnassa.domain.model

import java.io.Serializable

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