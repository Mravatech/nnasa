package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 2/26/2018.
 */
open class MnassaResponse(
        @SerializedName("status")
        val status: String) {

    val isOk: Boolean get() = status == "ok"
    val isFailed: Boolean get() = !isOk
}