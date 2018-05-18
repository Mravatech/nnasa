package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/5/2018.
 */
data class SendContactsRequest(
        @SerializedName("phones") val phones: List<String>,
        @SerializedName("needClean") val needClean: Boolean = false
)