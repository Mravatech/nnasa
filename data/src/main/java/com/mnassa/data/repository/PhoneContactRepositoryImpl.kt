package com.mnassa.data.repository

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.repository.ContactsRepository
import kotlinx.coroutines.experimental.async


/**
 * Created by Peter on 3/5/2018.
 */
class PhoneContactRepositoryImpl(private val contentResolver: ContentResolver) : ContactsRepository {

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun getPhoneContacts(): List<PhoneContact> {
        return async {
            val result = ArrayList<PhoneContact>()

            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER
            contentResolver.query(
                    uri,
                    arrayOf(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone._ID,
                            ContactsContract.Contacts._ID),
                    selection,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (!cursor.isAfterLast) {
                        result += PhoneContactImpl(phoneNumber = cursor.getString(numberColumnIndex))
                        cursor.moveToNext()
                    }
                }
            }

            //TODO: remove
            result.clear()
            result += PhoneContactImpl("380969478743")
            result += PhoneContactImpl("380969478743")
            result += PhoneContactImpl("380667520265")
            result += PhoneContactImpl("380667277832")
            result += PhoneContactImpl("380969478743")
            result += PhoneContactImpl("380951299232")
            result += PhoneContactImpl("380969478743")
            result += PhoneContactImpl("380677129504")
            result += PhoneContactImpl("380971760140")
            result += PhoneContactImpl("380971760140")
            result += PhoneContactImpl("380660482777")
            result += PhoneContactImpl("380937541581")
            result += PhoneContactImpl("380509445155")
            result += PhoneContactImpl("380675658651")
            result += PhoneContactImpl("380951299232")
            result += PhoneContactImpl("380951299232")
            result += PhoneContactImpl("380675658651")
            result += PhoneContactImpl("380935061405")
            result += PhoneContactImpl("380971760140")

            result
        }.await()
    }

    private data class PhoneContactImpl(override val phoneNumber: String) : PhoneContact

}



