package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.BuyTicketsRequest
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 4/19/2018.
 */
interface FirebaseEventsApi {
    @POST("/buyEventTickets")
    fun buyTickets(@Body request: BuyTicketsRequest): Deferred<MnassaResponse>
}