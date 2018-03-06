package com.mnassa.domain.interactor

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
interface InviteInteractor {

    suspend fun getPhoneConnections(): ReceiveChannel<List<ShortAccountModel>>

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    suspend fun sendPhoneContacts()

    suspend fun inviteUsers(userAccountIds: List<String>)
}