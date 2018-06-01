package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.ConnectionStatusRequest
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/30/2018
 */

interface FirebaseConnectionsApi {

    @POST("/connectionAction")
    fun connectionAction(@Body request: ConnectionStatusRequest): Deferred<Any>

}