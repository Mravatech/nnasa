package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 2/22/2018.
 */
internal open class ShortAccountDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("serialNumber") var serialNumber: Int?,
        @SerializedName("avatar") var avatar: String?,
        @SerializedName("firstName") var firstName: String?,
        @SerializedName("lastName") var lastName: String?,
        @SerializedName("organizationName") var organizationName: String?,
        @SerializedName("type") var type: String?,
        @SerializedName("userName") var userName: String?,
        @SerializedName("abilities") var abilitiesInternal: List<ShortAccountAbilityDbEntity>?,
        @SerializedName("connectedBy") var connectedBy: ConnectedByDbEntity? = null
) : HasIdMaybe {

    override fun toString(): String {
        return "ShortAccountDbEntity(id='$id', avatar=$avatar, firstName=$firstName, lastName=$lastName, organizationName=$organizationName, type='$type', userName='$userName', abilitiesInternal=$abilitiesInternal)"
    }
}

internal open class EventAttendeeAccountDbEntity(
        id: String,
        serialNumber: Int?,
        avatar: String?,
        firstName: String?,
        lastName: String?,
        organizationName: String?,
        type: String,
        userName: String,
        abilitiesInternal: List<ShortAccountAbilityDbEntity>?,
        connectedBy: ConnectedByDbEntity?,
        @SerializedName("presence") val presence: Boolean? = null
): ShortAccountDbEntity(id, serialNumber, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal, connectedBy)

internal open class DeclinedShortAccountDbEntity(
        id: String,
        serialNumber: Int?,
        avatar: String?,
        firstName: String?,
        lastName: String?,
        organizationName: String?,
        type: String,
        userName: String,
        abilitiesInternal: List<ShortAccountAbilityDbEntity>,
        @SerializedName("declinedAt") var declinedAt: Long
) : ShortAccountDbEntity(id, serialNumber, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {

    override fun toString(): String {
        return "DeclinedShortAccountDbEntity(declinedAt=$declinedAt); " + super.toString()
    }
}

internal data class ShortAccountAbilityDbEntity(
        @SerializedName("isMain") var isMain: Boolean?,
        @SerializedName("name") var name: String?,
        @SerializedName("place") var place: String?
)

data class ConnectedByDbEntity(
        @SerializedName("id") var id: String?,
        @SerializedName("type") var type: String?,
        @SerializedName("value") var value: String?
)