package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CustomTagsRequest
import com.mnassa.data.network.bean.retrofit.response.CustomTagsResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
interface FirebaseTagsApi {

    @POST("/tags")
    fun createCustomTagIds(@Body request: CustomTagsRequest): Deferred<CustomTagsResponse>

}