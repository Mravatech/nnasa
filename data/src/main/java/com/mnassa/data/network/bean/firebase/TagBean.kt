package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.mnassa.domain.models.Model

/**
 * Created by Peter on 2/22/2018.
 */
@IgnoreExtraProperties
data class TagBean(
        override var id: String,
        @PropertyName("ar")
        val ar: String,
        @PropertyName("en")
        val en: String,
        @PropertyName("status")
        val status: String) : Model {
    constructor() : this("", "", "", "")
}