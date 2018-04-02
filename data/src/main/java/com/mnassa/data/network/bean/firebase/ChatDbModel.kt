package com.mnassa.data.network.bean.firebase

import android.support.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

internal data class ChatDbModel(
        @SerializedName("id")
        @Nullable
        override var id: String,
        @SerializedName("unreadCount") var unreadCount: Int,
        @SerializedName("viewedAt") var viewedAt: String,
        @SerializedName("viewedAtDate") var viewedAtDate: Long,
        @SerializedName("lastMessage") var lastMessage: ChatLastMessageDbModel?,
        @SerializedName("members") var members: HashMap<String, Int>?
        ) : HasId

internal data class ChatLastMessageDbModel(
        @SerializedName("creator") var creator: String,
        @SerializedName("text") var text: String,
        @SerializedName("type") var type: String,
        @SerializedName("createdAtDate") var createdAtDate: String,
        @SerializedName("createdAt") var createdAt: Long
)

//internal data class ChatMembers(
//        @SerializedName("creator") var creator: String,
//        @SerializedName("text") var text: String,
//)