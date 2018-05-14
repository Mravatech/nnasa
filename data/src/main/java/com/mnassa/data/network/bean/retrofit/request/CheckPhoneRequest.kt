package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 2/26/2018.
 */
data class CheckPhoneRequest(
        @SerializedName("phone") val phone: String,
        @SerializedName("promoCode") val promoCode: String? = null
)