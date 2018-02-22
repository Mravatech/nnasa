package com.mnassa.data.network.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import java.util.*

/**
 * Created by Peter on 2/22/2018.
 */

@IgnoreExtraProperties
data class AccountBean(
        @PropertyName("avatar")
        val avatar: String,
        @PropertyName("createdAt")
        val createdAt: Long,
        @PropertyName("createdAtDate")
        val createdAtDate: Date,
        @PropertyName("invites")
        val invitesCount: Int,
        @PropertyName("language")
        val language: String,
        @PropertyName("userName")
        val userName: String
)