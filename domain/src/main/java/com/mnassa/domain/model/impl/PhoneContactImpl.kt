package com.mnassa.domain.model.impl

import com.mnassa.domain.model.PhoneContact

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */
data class PhoneContactImpl(
        override val phoneNumber: String,
        override val fullName: String,
        override val avatar: String?) : PhoneContact