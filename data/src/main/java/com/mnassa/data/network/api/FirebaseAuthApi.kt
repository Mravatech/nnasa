package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.*
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 2/21/2018.
 */
interface FirebaseAuthApi {
    @POST("/checkPhone")
    fun checkPhone(@Body request: CheckPhoneRequest): Deferred<MnassaResponse>

    @POST("/registerAccount")
    fun registerPersonalAccount(@Body request: RegisterPersonalAccountRequest): Deferred<RegisterAccountResponse>

    @POST("/registerAccount")
    fun registerOrganizationAccount(@Body request: RegisterOrganizationAccountRequets): Deferred<RegisterAccountResponse>

}