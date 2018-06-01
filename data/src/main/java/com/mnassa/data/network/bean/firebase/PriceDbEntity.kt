package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 5/4/2018.
 */
data class PriceDbEntity (
        @SerializedName("amount") val amount: Long,
        @SerializedName("state") val state: Boolean
)