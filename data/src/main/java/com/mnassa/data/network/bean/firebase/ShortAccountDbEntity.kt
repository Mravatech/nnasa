package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/22/2018.
 */

@IgnoreExtraProperties
internal open class ShortAccountDbEntity : HasId {
    override var id: String
    @PropertyName("avatar")
    var avatar: String?
    @PropertyName("firstName")
    var firstName: String?
    @PropertyName("lastName")
    var lastName: String?
    @PropertyName("organizationName")
    var organizationName: String?
    @PropertyName("type")
    var type: String
    @PropertyName("userName")
    var userName: String
    @PropertyName("abilities")
    var abilitiesInternal: List<ShortAccountAbilityDbEntity>

    constructor() : this("", null, "", "", null, "", "", emptyList())
    constructor(id: String, avatar: String?, firstName: String?, lastName: String?, organizationName: String?, type: String, userName: String, abilitiesInternal: List<ShortAccountAbilityDbEntity>) {
        this.id = id
        this.avatar = avatar
        this.firstName = firstName
        this.lastName = lastName
        this.organizationName = organizationName
        this.type = type
        this.userName = userName
        this.abilitiesInternal = abilitiesInternal
    }
}

internal open class InviteShortAccountDbEntity : ShortAccountDbEntity{
    @PropertyName("invites")
    var invites: Int

    constructor() : super() {
        this.invites = 0
    }

    constructor(id: String, avatar: String?, firstName: String?, lastName: String?, organizationName: String?, type: String, userName: String, abilitiesInternal: List<ShortAccountAbilityDbEntity>, invites: Int) : super(id, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {
        this.invites = invites
    }
}

@IgnoreExtraProperties
internal open class DeclinedShortAccountDbEntity : ShortAccountDbEntity {
    @PropertyName("declinedAt")
    var declinedAt: Long

    constructor() : super() {
        this.declinedAt = 0L
    }

    constructor(id: String, avatar: String?, firstName: String?, lastName: String?, organizationName: String?, type: String, userName: String, abilitiesInternal: List<ShortAccountAbilityDbEntity>, declinedAt: Long) : super(id, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {
        this.declinedAt = declinedAt
    }
}


@IgnoreExtraProperties
internal data class ShortAccountAbilityDbEntity(
        var id: String,
        var isMain: Boolean,
        var name: String?,
        var place: String?
) {
    constructor() : this("", false, null, null)
}