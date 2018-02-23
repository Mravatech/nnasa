package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.mnassa.domain.models.HasId

/**
 * Created by Peter on 2/23/2018.
 */
@IgnoreExtraProperties
data class TranslatedWordBean(
        override var id: String,

        @PropertyName("info")
        val eng: String?,
        @PropertyName("ar")
        val ar: String?
) : HasId {
    constructor() : this("", null, null)
}