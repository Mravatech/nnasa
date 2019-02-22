package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.ComplaintRequest
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */
interface FirebaseComplaintApi {

    @POST("/inappropriate")
    fun inappropriate(@Body request: ComplaintRequest): Deferred<Any>


}