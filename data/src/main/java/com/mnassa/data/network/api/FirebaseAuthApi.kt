package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.response.RegisterAccountInfoResponse
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import com.mnassa.data.network.bean.retrofit.response.RegisterAccountResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Created by Peter on 2/21/2018.
 */
interface FirebaseAuthApi {
    @POST("/checkPhone")
    fun checkPhone(@Body request: CheckPhoneRequest): Deferred<MnassaResponse>

    @POST("/registerAccount")
    fun registerPersonalAccount(@Body request: RegisterPersonalAccountRequest): Deferred<RegisterAccountResponse>

    @POST("/registerAccount")
    fun registerOrganizationAccount(@Body request: RegisterOrganizationAccountRequest): Deferred<RegisterAccountResponse>

    @PUT("/processAccount")
    fun registerSendAccountInfo(@Body request: RegisterSendingAccountInfoRequest): Deferred<RegisterAccountInfoResponse>

    @PUT("/processAccount")
    fun registerSendCompanyAccountInfo(@Body request: RegisterSendingCompanyAccountInfoRequest): Deferred<RegisterAccountInfoResponse>

    @PUT("/processAccount")
    fun profileUpdateCompanyAccountInfo(@Body request: ProfileCompanyAccountInfoRequest): Deferred<RegisterAccountInfoResponse>

    @PUT("/processAccount")
    fun profileUpdatePersonAccountInfo(@Body request: ProfilePersonAccountInfoRequest): Deferred<RegisterAccountInfoResponse>

    @POST("/addPushToken")
    fun addPushToken(@Body request: PushTokenRequest): Deferred<Any>

}