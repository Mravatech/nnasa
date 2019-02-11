package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mnassa.data.network.bean.firebase.adapters.InvitationAccountAvatarJsonAdapter
import com.mnassa.domain.model.HasId

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */
class InvitationDbEntity(
        @SerializedName("id") override var id: String = "",
        @SerializedName("createdAt") val createdAt: Long = 0L,
        @SerializedName("createdAtDate") val createdAtDate: String? = "",
        @SerializedName("description") val description: String? = null,
        @SerializedName("phone") val phone: String = "",
        @SerializedName("account") @JsonAdapter(InvitationAccountAvatarJsonAdapter::class) val avatar: String? = null,
        @SerializedName("used") val used: Boolean = false
) : HasId
