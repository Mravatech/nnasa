package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */
class InvitationDbEntity(
        @SerializedName("id") override var id: String,
        @PropertyName("createdAt") val createdAt: Long,
        @PropertyName("createdAtDate") val createdAtDate: String,
        @PropertyName("description") val description: String?,
        @PropertyName("phone") val phone: String,
        @PropertyName("used") val used: Boolean
) : HasId {
    constructor() : this("", 0L, "", null, "", false)
}