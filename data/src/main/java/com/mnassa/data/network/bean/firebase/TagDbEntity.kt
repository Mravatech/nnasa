package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/22/2018.
 */
internal data class TagDbEntity(
        @SerializedName("id") override var id: String,
        @SerializedName("ar") val ar: String,
        @SerializedName("en") val en: String,
        @SerializedName("status") val status: String) : HasId {
    constructor() : this("", "", "", "")
}