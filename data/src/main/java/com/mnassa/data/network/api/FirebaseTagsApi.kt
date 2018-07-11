package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.AddTagsDialogShowingTimeRequest
import com.mnassa.data.network.bean.retrofit.request.CustomTagsRequest
import com.mnassa.data.network.bean.retrofit.response.CustomTagsResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
interface FirebaseTagsApi {

    @POST("/tags")
    fun createCustomTagIds(@Body request: CustomTagsRequest): Deferred<CustomTagsResponse>

    @PUT("/processAccount")
    fun setAddTagsDialogShowingTime(@Body request: AddTagsDialogShowingTimeRequest): Deferred<MnassaResponse>

}