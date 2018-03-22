package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/22/2018.
 */

internal open class ShortAccountDbEntity : HasId {
    override var id: String = ""
    @SerializedName("avatar")
    var avatar: String?
    @SerializedName("firstName")
    var firstName: String?
    @SerializedName("lastName")
    var lastName: String?
    @SerializedName("organizationName")
    var organizationName: String?
    @SerializedName("type")
    var type: String
    @SerializedName("userName")
    var userName: String
    @SerializedName("abilities")
    var abilitiesInternal: List<ShortAccountAbilityDbEntity>?

    constructor(id: String, avatar: String?, firstName: String?, lastName: String?, organizationName: String?, type: String, userName: String, abilitiesInternal: List<ShortAccountAbilityDbEntity>?) {
        this.id = id
        this.avatar = avatar
        this.firstName = firstName
        this.lastName = lastName
        this.organizationName = organizationName
        this.type = type
        this.userName = userName
        this.abilitiesInternal = abilitiesInternal
    }

    override fun toString(): String {
        return "ShortAccountDbEntity(id='$id', avatar=$avatar, firstName=$firstName, lastName=$lastName, organizationName=$organizationName, type='$type', userName='$userName', abilitiesInternal=$abilitiesInternal)"
    }
}

internal open class DeclinedShortAccountDbEntity : ShortAccountDbEntity {
    @SerializedName("declinedAt")
    var declinedAt: Long

    constructor(id: String, avatar: String?, firstName: String?, lastName: String?, organizationName: String?, type: String, userName: String, abilitiesInternal: List<ShortAccountAbilityDbEntity>, declinedAt: Long) : super(id, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {
        this.declinedAt = declinedAt
    }

    override fun toString(): String {
        return "DeclinedShortAccountDbEntity(declinedAt=$declinedAt); " + super.toString()
    }
}

internal data class ShortAccountAbilityDbEntity(
        @SerializedName("isMain")
        var isMain: Boolean,
        @SerializedName("name")
        var name: String,
        @SerializedName("place")
        var place: String?
) {
}