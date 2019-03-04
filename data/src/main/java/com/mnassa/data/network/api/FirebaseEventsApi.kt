package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.BuyTicketsRequest
import com.mnassa.data.network.bean.retrofit.request.CreateOrEditEventRequest
import com.mnassa.data.network.bean.retrofit.request.EventAttendeeRequest
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Created by Peter on 4/19/2018.
 */
interface FirebaseEventsApi {
    @POST("/buyEventTickets")
    fun buyTickets(@Body request: BuyTicketsRequest): Deferred<MnassaResponse>

    @POST("/eventAttendee")
    fun saveAttendee(@Body request: EventAttendeeRequest): Deferred<MnassaResponse>

    @POST("/event")
    fun createEvent(@Body request: CreateOrEditEventRequest): Deferred<MnassaResponse>

    @PUT("/event")
    fun editEvent(@Body request: CreateOrEditEventRequest): Deferred<MnassaResponse>

    @PUT("/event")
    fun editEventStatus(@Body request: CreateOrEditEventRequest): Deferred<MnassaResponse>
}