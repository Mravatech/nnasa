package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CreateGroupRequest
import com.mnassa.data.network.bean.retrofit.request.GroupConnectionRequest
import com.mnassa.data.network.bean.retrofit.response.CreateGroupResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.*

/**
 * Created by Peter on 5/21/2018.
 */
interface FirebaseGroupsApi {

    @POST("manageCommunityMembers")
    fun inviteAction(@Body request: GroupConnectionRequest): Deferred<MnassaResponse>

    @POST("community")
    fun create(@Body request: CreateGroupRequest): Deferred<CreateGroupResponse>

    @PUT("community")
    fun update(@Body request: CreateGroupRequest): Deferred<MnassaResponse>

    @DELETE("community/{id}")
    fun delete(@Path("id") id: String): Deferred<MnassaResponse>
}