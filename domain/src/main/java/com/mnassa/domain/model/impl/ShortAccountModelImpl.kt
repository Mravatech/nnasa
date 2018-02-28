package com.mnassa.domain.model.impl

import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.OrganizationAccountDiffModel
import com.mnassa.domain.model.PersonalAccountDiffModel
import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 2/28/2018.
 */
class ShortAccountModelImpl : ShortAccountModel {
    override var id: String
    override var firebaseUserId: String
    override var userName: String
    override var accountType: AccountType
    override var avatar: String?
    override var contactPhone: String?
    override var language: String?
    override var personalInfo: PersonalAccountDiffModel?
    override var organizationInfo: OrganizationAccountDiffModel?

    constructor(
            id: String,
            firebaseUserId: String,
            userName: String,
            accountType: AccountType,
            avatar: String?,
            contactPhone: String?,
            language: String?,
            personalInfo: PersonalAccountDiffModel?,
            organizationInfo: OrganizationAccountDiffModel?
    ) {
        this.id = id
        this.firebaseUserId = firebaseUserId
        this.userName = userName
        this.accountType = accountType
        this.avatar = avatar
        this.contactPhone = contactPhone
        this.language = language
        this.personalInfo = personalInfo
        this.organizationInfo = organizationInfo
    }
}

class PersonalAccountDiffModelImpl : PersonalAccountDiffModel {
    override var firstName: String
    override var lastName: String

    constructor(firstName: String, lastName: String) {
        this.firstName = firstName
        this.lastName = lastName
    }
}

class OrganizationAccountDiffModelImpl : OrganizationAccountDiffModel {
    override var organizationName: String

    constructor(organizationName: String) {
        this.organizationName = organizationName
    }
}