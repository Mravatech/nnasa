package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/7/2018
 */
//todo remove class from future merge called RewardDbEntity
data class PaymentDbEntity(
        @SerializedName("amount") var amount: Long,
        @SerializedName("defaultDescription") var defaultDescription: String,
        @SerializedName("description") var description: String,
        @SerializedName("needAdmin") var needAdmin: Boolean,
        @SerializedName("state") var state: Boolean
)