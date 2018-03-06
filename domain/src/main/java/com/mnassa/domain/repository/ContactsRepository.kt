package com.mnassa.domain.repository

import android.Manifest
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.PhoneContact

/**
 * Created by Peter on 3/5/2018.
 */
interface ContactsRepository {

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    suspend fun getPhoneContacts(): List<PhoneContact>
}