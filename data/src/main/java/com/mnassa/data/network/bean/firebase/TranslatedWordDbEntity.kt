package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/23/2018.
 */
@IgnoreExtraProperties
internal data class TranslatedWordDbEntity(
        override var id: String,

        @PropertyName("info")
        val info: String,
        @PropertyName("ar")
        val ar: String?,
        @PropertyName("en")
        val en: String?
) : HasId {
    constructor() : this("", "", null, null)
}