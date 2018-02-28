package com.mnassa.domain.model

import android.os.Parcelable

/**
 * Created by Peter on 2/26/2018.
 */
interface PhoneVerificationModel : Parcelable {
    val phoneNumber: String
    val isVerified: Boolean
}