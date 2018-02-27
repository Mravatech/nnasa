package com.mnassa.screen.registration

import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 2/27/2018.
 */
open class FirstRegistrationStepData : Serializable {
    val accountType: AccountType
    val userName: String
    val city: String

    //personal
    var firstName: String? = null
    var secondName: String? = null

    //organization
    var companyName: String? = null

    constructor(userName: String, city: String, firstName: String, secondName: String) {
        this.accountType = AccountType.PERSONAL
        this.userName = userName
        this.city = city
        this.firstName = firstName
        this.secondName = secondName

    }

    constructor(userName: String, city: String, companyName: String) {
        this.accountType = AccountType.ORGANIZATION
        this.userName = userName
        this.city = city
        this.companyName = companyName
    }

    constructor(copy: FirstRegistrationStepData) {
        this.accountType = copy.accountType
        this.userName = copy.userName
        this.city = copy.city
        this.firstName = copy.firstName
        this.secondName = copy.secondName

        this.companyName = copy.companyName
    }
}

class SecondRegistrationStepData: FirstRegistrationStepData {

    var avatar: String? = null
    var dateOfBirth: Date? = null
    var gender: Gender? = null
    var socialStatus: SocialStatus? = null
    var phone: String? = null
    var showContactPhone: Boolean = false
    var email: String? = null
    var showContactEmail: Boolean = true
    var website: String? = null
    var landline: String? = null
    var language: String? = null
    var role: AccountRole = AccountRole.NONE

    var abilities: String? = null
    var offers: String? = null
    var interests: String? = null



    constructor(firstStep: FirstRegistrationStepData): super(firstStep) {

    }

    constructor(
            copy: FirstRegistrationStepData,
            avatar: String?,
            dateOfBirth: Date?,
            gender: Gender?,
            socialStatus: SocialStatus?,
            phone: String?,
            showContactPhone: Boolean,
            email: String?,
            showContactEmail: Boolean,
            website: String?,
            landline: String?,
            language: String?,
            role: AccountRole,
            abilities: String?,
            offers: String?,
            interests: String?
    ) : super(copy) {
        this.avatar = avatar
        this.dateOfBirth = dateOfBirth
        this.gender = gender
        this.socialStatus = socialStatus
        this.phone = phone
        this.showContactPhone = showContactPhone
        this.email = email
        this.showContactEmail = showContactEmail
        this.website = website
        this.landline = landline
        this.language = language
        this.role = role
        this.abilities = abilities
        this.offers = offers
        this.interests = interests
    }
}

enum class AccountType {
    PERSONAL, ORGANIZATION
}

enum class AccountRole {
    NONE, VALUE_CENTER
}

enum class Gender {
    MALE, FEMALE
}

enum class SocialStatus {

}

enum class OrganizationType {
    COMMERCIAL, GOVERNMENT, NON_PROFIT
}