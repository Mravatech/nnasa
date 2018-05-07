package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
data class RewardDbEntity(
        @SerializedName("amount") var amount: Int,
        @SerializedName("defaultDescription") var defaultDescription: String,
        @SerializedName("description") var description: String,
        @SerializedName("needAdmin") var needAdmin: Boolean,
        @SerializedName("state") var state: Boolean
)