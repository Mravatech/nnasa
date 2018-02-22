package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.firebase.auth.FirebaseUser
import com.mnassa.domain.models.impl.UserProfileImpl

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertUser)
    }

    private fun convertUser(input: FirebaseUser): UserProfileImpl {
        return UserProfileImpl(
                id = input.uid,
                name = input.displayName ?: ""
        )
    }
}