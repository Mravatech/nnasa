package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 5/21/2018.
 */
internal data class GroupDbEntity(
        @SerializedName("id") override var id: String,
        @SerializedName("avatar") val avatar: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("isAdmin") val isAdmin: Boolean?,
        @SerializedName("title") val title: String?,
        @SerializedName("createdAt") val createdAt: Long?,
        @SerializedName("counters") val counters: GroupCounters?,
        @SerializedName("admins") val admins: List<String>?,
        @SerializedName("author") val author: ShortAccountDbEntity?,
        @SerializedName("website") val website: String?,
        @SerializedName("location") var location: LocationDbEntity?
) : HasId

internal data class GroupCounters(
        @SerializedName("numberOfParticipants") val numberOfParticipants: Long?
)