package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.bean.retrofit.response.CreatePostResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.*

/**
 * Created by Peter on 3/16/2018.
 */
interface FirebasePostApi {
    @POST("/post")
    fun createPost(@Body request: CreatePostRequest): Deferred<CreatePostResponse>

    @PUT("/post")
    fun changePost(@Body request: CreatePostRequest): Deferred<MnassaResponse>

    @DELETE("/post/{postId}")
    fun deletePost(@Path("postId") postId: String): Deferred<MnassaResponse>

    @POST("/itemView")
    fun viewItems(@Body request: ViewItemsRequest): Deferred<MnassaResponse>

    @POST("/itemOpen")
    fun openItem(@Body request: OpenItemsRequest): Deferred<MnassaResponse>

    @POST("/repost")
    fun repostComment(@Body request: RepostCommentRequest): Deferred<CreatePostResponse>

    @POST("/unpinInfoPost")
    fun hideInfoPost(@Body request: HideInfoPostRequest): Deferred<MnassaResponse>
}