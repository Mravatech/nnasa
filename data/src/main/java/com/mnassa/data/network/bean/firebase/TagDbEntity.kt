package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 2/22/2018.
 */
internal data class TagDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("ar") val ar: String?,
        @SerializedName("en") val en: String?,
        @SerializedName("status") val status: String?) : HasIdMaybe