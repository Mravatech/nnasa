package com.mnassa.data.network.bean.firebase

import android.support.annotation.Nullable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
internal data class NotificationDbEntity(
        @SerializedName("id") @Nullable override var id: String,
        @SerializedName(PROPERTY_CREATED_AT) var createdAt: Long,
        @SerializedName("text") var text: String,
        @SerializedName("type") var type: String,
        @SerializedName("extra") var extra: NotificationExtraDbEntity?
) : HasId {
    companion object {
        const val PROPERTY_CREATED_AT = "createdAt"
    }
}

internal data class NotificationExtraDbEntity(
        @SerializedName("author") var author: JsonObject,
        @SerializedName("post") var post: JsonObject,
        @SerializedName("reffered") var reffered: JsonObject?,
        @SerializedName("recommended") var recommended: JsonObject?,
        @SerializedName("community") val group: JsonObject?,
        @SerializedName("eventName") val eventName: String?,
        @SerializedName("ticketsPrice") val ticketsPrice: String?,
        @SerializedName("totalPrice") val totalPrice: String?,
        @SerializedName("attendee") val attendee: String?,
        @SerializedName("event") val event: JsonObject?,
        @SerializedName("newInviteNumber") val newInviteNumber: Int?
)
