package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */

data class PushSettingResponse(
        @SerializedName("data")
        internal var data: AccountPushSettingData
) : MnassaResponse()

data class AccountPushSettingData(
        @SerializedName("accountPushSettings")
        val accountPushSettings: HashMap<String, PushSettingData>
)

data class PushSettingData(
        @SerializedName("isActive") var isActive: Boolean,
        @SerializedName("withSound") var withSound: Boolean
)