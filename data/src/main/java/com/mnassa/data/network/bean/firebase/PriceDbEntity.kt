package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 5/4/2018.
 */
data class PriceDbEntity(
    @SerializedName("amount") val amountOrNull: Long?,
    @SerializedName("state") val stateOrNull: Boolean?
) {
    val amount: Long
        get() = amountOrNull ?: 0
    val state: Boolean
        get() = stateOrNull ?: false
}