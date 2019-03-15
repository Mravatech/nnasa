package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 5/21/2018.
 */
internal data class GroupDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("avatar") val avatar: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("isAdmin") val isAdmin: Boolean?,
        @SerializedName("title") val title: String?,
        @SerializedName("createdAt") val createdAt: Long?,
        @SerializedName("counters") val counters: GroupCounters?,
        @SerializedName("admins") val admins: List<String>?,
        @SerializedName("author") val author: ShortAccountDbEntity?,
        @SerializedName("website") val website: String?,
        @SerializedName("location") var location: LocationDbEntity?,
        @SerializedName("tags") var tags: List<String>?,

        @SerializedName("permissions") var permissions: GroupPermissionsEntity?,

        @SerializedName("points") val points: Long? = null,
        @SerializedName("totalIncome") val totalIncome: Long? = null,
        @SerializedName("totalOutcome") val totalOutcome: Long? = null,
        @SerializedName("visiblePoints") val visiblePoints: Long? = null

) : HasIdMaybe

internal data class GroupCounters(
        @SerializedName("numberOfParticipants") val numberOfParticipants: Long?,
        @SerializedName("numberOfInvites") val numberOfInvites: Long?
)

data class GroupPermissionsEntity(
        @SerializedName("createAccountPost") val canCreateAccountPostOrNull: Boolean? = null,
        @SerializedName("createEvent") val canCreateEventOrNull: Boolean? = null,
        @SerializedName("createGeneralPost") val canCreateGeneralPostOrNull: Boolean? = null,
        @SerializedName("createNeedPost") val canCreateNeedPostOrNull: Boolean? = null,
        @SerializedName("createOfferPost") val canCreateOfferPostOrNull: Boolean? = null
) {
        val canCreateAccountPost: Boolean
                get() = canCreateAccountPostOrNull ?: false
        val canCreateEvent: Boolean
                get() = canCreateEventOrNull ?: false
        val canCreateGeneralPost: Boolean
                get() = canCreateGeneralPostOrNull ?: false
        val canCreateNeedPost: Boolean
                get() = canCreateNeedPostOrNull ?: false
        val canCreateOfferPost: Boolean
                get() = canCreateOfferPostOrNull ?: false
}