package com.mnassa.domain.model

/**
 * Created by Peter on 3/5/2018.
 */
interface PhoneContact {
    val fullName: String
    val phoneNumber: String
    val avatar: String?
}

interface PhoneContactInvited : PhoneContact {
    val description: String?
    val createdAt: Long
    val createdAtDate: String
    val used : Boolean
}