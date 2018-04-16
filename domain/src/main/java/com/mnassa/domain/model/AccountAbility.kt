package com.mnassa.domain.model

import android.os.Parcelable
import java.io.Serializable
/**
 * Created by Peter on 3/5/2018.
 */

interface AccountAbility : Parcelable,Serializable {
    val isMain: Boolean
    val name: String?
    val place: String?
}