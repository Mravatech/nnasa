package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.CheckSmsRequest
import com.mnassa.data.network.bean.retrofit.request.SendSmsRequest
import com.mnassa.data.network.bean.retrofit.response.CheckSmsResponse
import com.mnassa.data.network.bean.retrofit.response.SendSmsResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 9/25/2018.
 */
interface CustomAuthApi {

    @POST("/sendSms")
    fun sendSms(@Body request: SendSmsRequest): Deferred<SendSmsResponse>

    @POST("/checkSms")
    fun checkSms(@Body request: CheckSmsRequest): Deferred<CheckSmsResponse>
}