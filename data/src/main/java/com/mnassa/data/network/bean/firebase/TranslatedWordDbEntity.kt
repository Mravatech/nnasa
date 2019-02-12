package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 2/23/2018.
 */
internal data class TranslatedWordDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("info") val info: String?,
        @SerializedName("ar") val ar: String?,
        @SerializedName("en") val en: String?
) : HasIdMaybe