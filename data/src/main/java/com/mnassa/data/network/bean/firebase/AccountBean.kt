package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.AccountModel

/**
 * Created by Peter on 2/22/2018.
 */

@IgnoreExtraProperties
internal data class AccountBean(
        override var id: String,
        @PropertyName("avatar")
        override var avatar: String?,
        @PropertyName("firstName")
        override var firstName: String,
        @PropertyName("lastName")
        override var lastName: String,
        @PropertyName("organizationName")
        override var organizationName: String?,
        @PropertyName("type")
        override var type: String,
        @PropertyName("userName")
        override var userName: String,
        @PropertyName("abilities")
        var abilitiesInternal: List<AccountAbilityBean>
) : AccountModel {
    override var abilities: List<AccountAbility> = emptyList()
        get() = abilitiesInternal
}

@IgnoreExtraProperties
internal data class AccountAbilityBean(
        override var id: String,
        override var isMain: Boolean,
        override var name: String?,
        override var place: String
) : AccountAbility