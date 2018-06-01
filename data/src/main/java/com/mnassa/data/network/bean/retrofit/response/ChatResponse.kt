package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/4/2018
 */

class ChatResponse : MnassaResponse() {
    @SerializedName("data") internal lateinit var data: ChatRoom
}

data class ChatRoom(
        @SerializedName("chatID") val chatID: String,
        @SerializedName("chatType") val chatType: String
)
