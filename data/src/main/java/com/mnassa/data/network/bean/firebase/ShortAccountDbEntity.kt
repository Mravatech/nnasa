package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.mnassa.domain.model.HasId
import com.mnassa.domain.model.Model

/**
 * Created by Peter on 2/22/2018.
 */

@IgnoreExtraProperties
internal data class ShortAccountDbEntity(
        override var id: String,
        @PropertyName("avatar")
        var avatar: String?,
        @PropertyName("firstName")
        var firstName: String?,
        @PropertyName("lastName")
        var lastName: String?,
        @PropertyName("organizationName")
        var organizationName: String?,
        @PropertyName("type")
        var type: String,
        @PropertyName("userName")
        var userName: String,
        @PropertyName("abilities")
        var abilitiesInternal: List<ShortAccountAbilityDbEntity>
): HasId {

    constructor() : this("", null, "", "", null, "", "", emptyList())
}

@IgnoreExtraProperties
internal data class ShortAccountAbilityDbEntity(
        var id: String,
        var isMain: Boolean,
        var name: String?,
        var place: String?
) {
    constructor() : this("", false, null, null)
}