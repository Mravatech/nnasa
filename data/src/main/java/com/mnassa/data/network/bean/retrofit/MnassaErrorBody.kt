package com.mnassa.data.network.bean.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 2/26/2018.
 */
data class MnassaErrorBody(
        @SerializedName("status")
        val status: String,
        @SerializedName("errorCode")
        val errorCode: String,
        @SerializedName("error")
        val error: String
)