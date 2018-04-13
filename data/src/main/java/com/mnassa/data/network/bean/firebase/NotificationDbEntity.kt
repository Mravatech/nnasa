package com.mnassa.data.network.bean.firebase

import android.support.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
internal data class NotificationDbEntity(
        @SerializedName("id")
        @Nullable
        override var id: String,
        @SerializedName("createdAt") var createdAt: Long,
        @SerializedName("text") var text: String,
        @SerializedName("type") var type: String,
        @SerializedName("extra") var extra: NotificationAuthorDbEntity?
) : HasId

internal data class NotificationAuthorDbEntity(
        @SerializedName("author") var author: HashMap<String, ShortAccountDbEntity>
)
