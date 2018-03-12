package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/2/2018.
 */
data class RegisterUiKeyRequest(
        @SerializedName("keys")
        val keys: List<UiKey>
) {
    constructor(key: String, info: String) : this(listOf(UiKey(key, info)))
}

data class UiKey(@SerializedName("key")
                 val key: String,
                 @SerializedName("info")
                 val info: String)