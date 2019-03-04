package com.mnassa.data.repository

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.annotation.RequiresPermission
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.impl.PhoneContactImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.repository.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by Peter on 3/5/2018.
 */
class PhoneContactRepositoryImpl(private val contentResolver: ContentResolver,
                                 private val appInfoProvider: AppInfoProvider) : ContactsRepository {

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun getPhoneContacts(): List<PhoneContact> {
        return withContext(Dispatchers.Default) {
            val result = ArrayList<PhoneContact>()

            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER
            contentResolver.query(
                    uri,
                    arrayOf(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone._ID,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                            ContactsContract.Contacts._ID),
                    selection,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val avatarColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    val regex = Regex("[^0-9 ]")
                    while (!cursor.isAfterLast) {
                        val phone = regex.replace(cursor.getString(numberColumnIndex), "")
                        if (phone.length > NUMBER_LENGTH) {
                            result += PhoneContactImpl(
                                    phoneNumber = phone
                                            .replace(" ", "")
                                            .replace("-", "")
                                            .replace("(", "")
                                            .replace(")", ""),
                                    fullName = cursor.getString(nameColumnIndex),
                                    avatar = cursor.getString(avatarColumnIndex))
                        }
                        cursor.moveToNext()
                    }
                }
            }

            result.distinctBy { it.phoneNumber }
        }
    }

    companion object {
        private const val NUMBER_LENGTH = 11
    }

}



