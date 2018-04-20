package com.mnassa.data.network.api

import com.mnassa.data.network.bean.retrofit.request.PushSettingsRequest
import com.mnassa.data.network.bean.retrofit.response.PushSettingResponse
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.Body
import retrofit2.http.PUT

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/19/2018
 */
interface FirebaseSettingsApi {

    @PUT("/accountNotifications")
    fun accountNotifications(@Body fieldMap: Map<String, PushSettingsRequest>): Deferred<PushSettingResponse>

}