package com.mnassa.domain.model

import android.os.Parcelable
import java.io.Serializable

/**
 * Created by Peter on 2/26/2018.
 */
interface PhoneVerificationModel : Parcelable, Serializable {
    val phoneNumber: String
    val isVerified: Boolean
}