package com.mnassa.domain.model.impl

import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */
data class PhoneContactImpl(
        override val phoneNumber: String,
        override val fullName: String,
        override val avatar: String?) : PhoneContact

data class PhoneContactInvitedImpl(
        override val phoneNumber: String,
        override val avatar: String?,
        override val createdAt: Long,
        override val createdAtDate: String,
        override val description: String?,
        override val used: Boolean
): PhoneContactInvited{
    override val fullName: String = ""
}