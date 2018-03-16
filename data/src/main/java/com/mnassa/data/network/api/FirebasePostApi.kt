package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CreatePostRequest
import com.mnassa.data.network.bean.retrofit.request.ViewItemsRequest
import com.mnassa.data.network.bean.retrofit.response.CreatePostResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 3/16/2018.
 */
interface FirebasePostApi {
    @POST("/post")
    fun createPost(@Body request: CreatePostRequest): Deferred<CreatePostResponse>

    @POST("/itemView")
    fun viewItems(@Body request: ViewItemsRequest): Deferred<MnassaResponse>
}