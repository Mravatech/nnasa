package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 3/30/2018.
 */
internal data class TransactionDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("afterBalance") var afterBalance: Long?,
        @SerializedName("amount") var amount: Long?,
        @SerializedName("transactionAt") var transactionAt: Long?,
        @SerializedName("type") internal var type: String?,
        @SerializedName("by") internal var by: Map<String, TransactionMemberDbEntity>?,
        @SerializedName("from") internal var from: Map<String, TransactionMemberDbEntity>?,
        @SerializedName("to") internal var to: Map<String, TransactionMemberDbEntity>?,
        @SerializedName("userDescription") internal var userDescription: String?

/*
 fromType, toType, byType == 'account' | 'community'
 */
): HasIdMaybe

internal data class TransactionMemberDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("firstName") var firstName: String?,
        @SerializedName("lastName") var lastName: String?,
        @SerializedName("organizationName") var organizationName: String?,
        @SerializedName("title") val title: String?
): HasIdMaybe