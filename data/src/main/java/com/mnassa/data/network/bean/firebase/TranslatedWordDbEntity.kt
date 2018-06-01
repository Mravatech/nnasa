package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/23/2018.
 */
internal data class TranslatedWordDbEntity(
        @SerializedName("id") override var id: String,
        @SerializedName("info") val info: String,
        @SerializedName("ar") val ar: String?,
        @SerializedName("en") val en: String?
) : HasId {
    constructor() : this("", "", null, null)
}