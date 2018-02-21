package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.repository.SomInternalUserProfile
import com.mnassa.domain.models.impl.UserProfileImpl

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertUser)
    }

    private fun convertUser(input: SomInternalUserProfile): UserProfileImpl {
        return UserProfileImpl(
                id = input.id.toString(),
                name = input.name
        )
    }
}