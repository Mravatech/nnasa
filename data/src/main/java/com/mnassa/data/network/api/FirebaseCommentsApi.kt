package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CreateCommentRequest
import com.mnassa.data.network.bean.retrofit.request.GetCommentsRequest
import com.mnassa.data.network.bean.retrofit.request.RepostCommentRequest
import com.mnassa.data.network.bean.retrofit.response.CreateCommentResponse
import com.mnassa.data.network.bean.retrofit.response.GetCommentsResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.*

/**
 * Created by Peter on 3/26/2018.
 */
interface FirebaseCommentsApi {
    @POST("/comments")
    fun getComments(@Body request: GetCommentsRequest): Deferred<GetCommentsResponse>

    @POST("/comment")
    fun createComment(@Body request: CreateCommentRequest): Deferred<CreateCommentResponse>

    @DELETE("/comment/{commentId}")
    fun deleteComment(@Path("commentId") commentId: String): Deferred<MnassaResponse>

}