package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.RegisterUiKeyRequest
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by Peter on 3/2/2018.
 */
interface FirebaseDictionaryApi {
    @POST("/registerUiKey")
    fun registerUiKey(@Body request: RegisterUiKeyRequest): Deferred<Any>
}