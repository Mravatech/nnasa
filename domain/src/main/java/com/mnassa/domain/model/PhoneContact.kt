package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 3/5/2018.
 */
interface PhoneContact : Serializable {
    val fullName: String
    val phoneNumber: String
    val avatar: String?
}

interface PhoneContactInvited : PhoneContact {
    val description: String?
    val createdAt: Long
    val used: Boolean
}