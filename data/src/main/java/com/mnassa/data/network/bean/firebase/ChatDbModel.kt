package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

internal data class ChatDbModel(
        @SerializedName("unreadCount") var unreadCount: Int?,
        @SerializedName("viewedAtDate") var viewedAtDate: Long?,
        @SerializedName("lastMessage") var lastMessage: ChatMessageDbModel?,
        @SerializedName("members") var members: HashMap<String, Int>?,
        @SerializedName("id") override var idOrNull: String?
        ) : HasIdMaybe

internal data class ChatMessageDbModel(
        @SerializedName("creator") var creator: String?,
        @SerializedName("text") var text: String?,
        @SerializedName("type") var type: String?,
        @SerializedName("createdAt") var createdAt: Long?,
        @SerializedName("chatID") var chatID: String?,
        @SerializedName("linkedMessageId") var linkedMessageId: String?,
        @SerializedName("linkedPostId") var linkedPostId: String?,
        @SerializedName("id") override var idOrNull: String?
) : HasIdMaybe