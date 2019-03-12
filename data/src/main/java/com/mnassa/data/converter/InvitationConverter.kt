package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.InvitationDbEntity
import com.mnassa.domain.model.PhoneContactInvited
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
                phoneNumber = input.phone ?: CONVERT_ERROR_MESSAGE,
                avatar = input.avatar,
                createdAt = input.createdAt ?: PhoneContactInvited.DEFAULT_CREATED_AT_DATE,
                description = input.description,
                used = input.used ?: PhoneContactInvited.DEFAULT_USED)
    }
}