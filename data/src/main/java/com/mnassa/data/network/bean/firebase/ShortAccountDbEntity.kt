package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/22/2018.
 */

internal open class ShortAccountDbEntity(
        override var id: String,
        @SerializedName("avatar") var avatar: String?,
        @SerializedName("firstName") var firstName: String?,
        @SerializedName("lastName") var lastName: String?,
        @SerializedName("organizationName") var organizationName: String?,
        @SerializedName("type") var type: String,
        @SerializedName("userName") var userName: String,
        @SerializedName("abilities") var abilitiesInternal: List<ShortAccountAbilityDbEntity>?
) : HasId {

    override fun toString(): String {
        return "ShortAccountDbEntity(id='$id', avatar=$avatar, firstName=$firstName, lastName=$lastName, organizationName=$organizationName, type='$type', userName='$userName', abilitiesInternal=$abilitiesInternal)"
    }
}

internal open class DeclinedShortAccountDbEntity(
        id: String,
        avatar: String?,
        firstName: String?,
        lastName: String?,
        organizationName: String?,
        type: String,
        userName: String,
        abilitiesInternal: List<ShortAccountAbilityDbEntity>,
        @SerializedName("declinedAt") var declinedAt: Long
) : ShortAccountDbEntity(id, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {

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
)