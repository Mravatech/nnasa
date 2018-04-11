package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 3/30/2018.
 */
internal data class TransactionDbEntity(
        override var id: String,
        @SerializedName("afterBalance") var afterBalance: Long,
        @SerializedName("amount") var amount: Long,
        @SerializedName("transactionAt") var transactionAt: Long,
        @SerializedName("type") internal var type: String,
        @SerializedName("by") internal var by: Map<String, ShortAccountDbEntity>?,
        @SerializedName("from") internal var from: Map<String, ShortAccountDbEntity>?,
        @SerializedName("to") internal var to: Map<String, ShortAccountDbEntity>?,
        @SerializedName("userDescription") internal var userDescription: String?
): HasId