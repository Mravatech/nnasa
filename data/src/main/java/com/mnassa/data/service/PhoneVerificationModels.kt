package com.mnassa.data.service

import com.google.firebase.auth.PhoneAuthProvider
import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by Peter on 9/25/2018.
 */
@Parcelize
internal class OnVerificationCompleted(
        override val phoneNumber: String,
        override val isVerified: Boolean = true) : PhoneVerificationModel

@Parcelize
internal class OnCodeSent(
        override val phoneNumber: String,
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken?,
        override val isVerified: Boolean = false) : PhoneVerificationModel

@Parcelize
internal class EmailAuth(
        override val phoneNumber: String = "",
        override val isVerified: Boolean = true
) : PhoneVerificationModel