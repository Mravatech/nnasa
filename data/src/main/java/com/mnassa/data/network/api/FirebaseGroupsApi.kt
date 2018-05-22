package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.GroupConnectionRequest
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 5/21/2018.
 */
interface FirebaseGroupsApi {

    @POST("communityInviteAction")
    fun inviteAction(@Body request: GroupConnectionRequest): Deferred<MnassaResponse>
}