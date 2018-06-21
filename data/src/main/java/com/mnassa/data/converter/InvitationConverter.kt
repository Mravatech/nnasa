package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.InvitationDbEntity
import com.mnassa.domain.model.impl.PhoneContactInvitedImpl

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */
class InvitationConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convert)
    }

    private fun convert(input: InvitationDbEntity): PhoneContactInvitedImpl {
        return PhoneContactInvitedImpl(
                phoneNumber = input.phone,
                avatar = null,
                createdAt = input.createdAt,
                createdAtDate = input.createdAtDate,
                description = input.description,
                used = input.used)
    }
}