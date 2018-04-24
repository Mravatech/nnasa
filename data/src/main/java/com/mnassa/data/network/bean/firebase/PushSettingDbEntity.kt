package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */

data class PushSettingDbEntity(
        @SerializedName("id")
        override var id: String,
        @SerializedName("isActive") var isActive: Boolean,
        @SerializedName("withSound") var withSound: Boolean
) : HasId