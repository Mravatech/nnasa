package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CreateGroupRequest
import com.mnassa.data.network.bean.retrofit.request.DeleteGroupRequest
import com.mnassa.data.network.bean.retrofit.request.GroupConnectionRequest
import com.mnassa.data.network.bean.retrofit.response.CreateGroupResponse
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

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

    @HTTP(method = "DELETE", path = "/community", hasBody = true)
    fun delete(@Body request: DeleteGroupRequest): Deferred<MnassaResponse>
}