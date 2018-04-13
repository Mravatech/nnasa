package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.NotificationViewRequest
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
interface FirebaseNotificationsApi {

    @POST("/notificationView")
    fun notificationView(@Body request: NotificationViewRequest): Deferred<Any>

}