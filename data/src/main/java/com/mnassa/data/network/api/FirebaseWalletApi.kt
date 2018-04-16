package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.SendPointsRequest
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 3/30/2018.
 */
interface FirebaseWalletApi {

    @POST("sendPoints")
    fun sendPoints(@Body request: SendPointsRequest): Deferred<MnassaResponse>

}