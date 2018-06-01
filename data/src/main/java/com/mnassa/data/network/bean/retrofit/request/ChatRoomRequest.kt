package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/4/2018
 */

data class ChatRoomRequest(
        @SerializedName("userAID") val userAID: String
)

data class MessageRequest(
        @SerializedName("type") val type: String,
        @SerializedName("text") val text: String,
        @SerializedName("chatId") val chatID: String,
        @SerializedName("linkedMessageId") val linkedMessageId: String?,
        @SerializedName("linkedPostId") val linkedPostId: String?
)

data class ChatUnreadCountRequest(
        @SerializedName("chatID") val chatID: String
)

data class MessageFromChatRequest(
        @SerializedName("id") val id: String,
        @SerializedName("chatId") val chatID: String,
        @SerializedName("removeForAll") val forAll: Boolean
)