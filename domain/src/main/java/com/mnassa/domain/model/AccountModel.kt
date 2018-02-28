package com.mnassa.domain.model

/**
 * Created by Peter on 2/26/2018.
 */
interface AccountModel : Model {
    var avatar: String?
    var firstName: String
    var lastName: String
    var organizationName: String?
    var type: String
    var userName: String
    var abilities: List<AccountAbility>
}

interface AccountAbility : Model {
    var isMain: Boolean
    var name: String?
    var place: String
}