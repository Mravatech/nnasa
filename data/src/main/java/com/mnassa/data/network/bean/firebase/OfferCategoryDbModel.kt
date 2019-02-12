package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 5/3/2018.
 */
data class OfferCategoryDbModel(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("ar") val ar: String?,
        @SerializedName("en") val en: String,
        @SerializedName("parentId") val parentId: String?
) : HasIdMaybe