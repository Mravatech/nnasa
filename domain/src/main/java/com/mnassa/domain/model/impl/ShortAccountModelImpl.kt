package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*

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
    override var abilities: List<AccountAbility>

    constructor(
            id: String,
            firebaseUserId: String,
            userName: String,
            accountType: AccountType,
            avatar: String?,
            contactPhone: String?,
            language: String?,
            personalInfo: PersonalAccountDiffModel?,
            organizationInfo: OrganizationAccountDiffModel?,
            abilities: List<AccountAbility>
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
        this.abilities = abilities
    }
}

class PersonalAccountDiffModelImpl(override var firstName: String, override var lastName: String) : PersonalAccountDiffModel

class OrganizationAccountDiffModelImpl(override var organizationName: String) : OrganizationAccountDiffModel