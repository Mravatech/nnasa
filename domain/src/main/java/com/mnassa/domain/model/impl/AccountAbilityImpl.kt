package com.mnassa.domain.model.impl

import com.mnassa.domain.model.AccountAbility
import kotlinx.android.parcel.Parcelize

/**
 * Created by Peter on 3/5/2018.
 */
@Parcelize
data class AccountAbilityImpl(override val isMain: Boolean, override val name: String?, override val place: String?) : AccountAbility