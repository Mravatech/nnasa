package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.ConnectionActionRequest
import com.mnassa.data.network.bean.retrofit.request.SendContactsRequest
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 3/5/2018.
 */
interface FirebaseInviteApi {
    @POST("/getMyConnections")
    fun sendContacts(@Body request: SendContactsRequest): Deferred<MnassaResponse>

    @POST("/connectionAction")
    fun executeConnectionAction(@Body request: ConnectionActionRequest): Deferred<MnassaResponse>
}