package com.mnassa.domain.model

import android.os.Parcelable

/**
 * Created by Peter on 3/5/2018.
 */

interface AccountAbility : Parcelable {
    val isMain: Boolean
    val name: String?
    val place: String?
}