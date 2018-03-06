package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/2/2018.
 */
data class RegisterUiKeyRequest(
        @SerializedName("key")
        val key: String,
        @SerializedName("info")
        val info: String
)