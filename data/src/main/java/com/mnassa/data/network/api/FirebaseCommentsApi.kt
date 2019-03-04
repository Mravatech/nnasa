package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CreateCommentRequest
import com.mnassa.data.network.bean.retrofit.request.DeleteCommentRequest
import com.mnassa.data.network.bean.retrofit.request.EditCommentRequest
import com.mnassa.data.network.bean.retrofit.request.GetCommentsRequest
import com.mnassa.data.network.bean.retrofit.response.CreateCommentResponse
import com.mnassa.data.network.bean.retrofit.response.GetCommentsResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Created by Peter on 3/26/2018.
 */
interface FirebaseCommentsApi {
    @POST("/comments")
    fun getComments(@Body request: GetCommentsRequest): Deferred<GetCommentsResponse>

    @POST("/comment")
    fun createComment(@Body request: CreateCommentRequest): Deferred<CreateCommentResponse>

//    @DELETE("/comment/{commentId}")
    @HTTP(method = "DELETE", path = "/comment", hasBody = true)
    fun deleteComment(@Body request: DeleteCommentRequest): Deferred<MnassaResponse>

    @PUT("/comment")
    fun editComment(@Body request: EditCommentRequest): Deferred<MnassaResponse>

}