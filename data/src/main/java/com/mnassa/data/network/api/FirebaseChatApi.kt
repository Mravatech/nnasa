package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.ChatRoomRequest
import com.mnassa.data.network.bean.retrofit.request.ChatUnreadCountRequest
import com.mnassa.data.network.bean.retrofit.request.MessageFromChatRequest
import com.mnassa.data.network.bean.retrofit.request.MessageRequest
import com.mnassa.data.network.bean.retrofit.response.ChatResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/4/2018
 */

interface FirebaseChatApi {

    @POST("/addChat")
    fun addChat(@Body request: ChatRoomRequest): Deferred<ChatResponse>

    @POST("/message")
    fun sendMessage(@Body request: MessageRequest): Deferred<Any>

    @HTTP(method = "DELETE", path = "/message", hasBody = true)
    fun deleteMessage(@Body request: MessageFromChatRequest): Deferred<Any>

    @POST("/resetChatUnreadCount")
    fun resetChatUnreadCount(@Body request: ChatUnreadCountRequest): Deferred<Any>

}