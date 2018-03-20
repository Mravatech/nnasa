package com.mnassa.data.repository

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import android.support.annotation.RequiresPermission
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.impl.PhoneContactImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.repository.ContactsRepository
import kotlinx.coroutines.experimental.async


/**
 * Created by Peter on 3/5/2018.
 */
class PhoneContactRepositoryImpl(private val contentResolver: ContentResolver,
                                 private val appInfoProvider: AppInfoProvider) : ContactsRepository {

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
                            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                            ContactsContract.Contacts._ID),
                    selection,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val avatarColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                    while (!cursor.isAfterLast) {
                        if (cursor.getString(numberColumnIndex).length > NUMBER_WITHOUT_CODE) {
                            val phone = cursor.getString(numberColumnIndex)
                            result += PhoneContactImpl(
                                    phoneNumber = phone.substring(phone.length - NUMBER_WITHOUT_CODE),
                                    fullName = cursor.getString(nameColumnIndex),
                                    avatar = cursor.getString(avatarColumnIndex))
                        }
                        cursor.moveToNext()
                    }
                }
            }
//            if (appInfoProvider.isDebug) {
//                result.clear()
//                result.addAll(getDebugPhoneList())
//            }

            result.distinctBy { it.phoneNumber }
        }.await()
    }


    private fun getDebugPhoneList(): List<PhoneContactImpl> {
        val result = ArrayList<PhoneContactImpl>()
        result += PhoneContactImpl("380969478743", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380969478743", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380667520265", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380667277832", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380969478743", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380951299232", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380969478743", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380677129504", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380971760140", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380971760140", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380660482777", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380937541581", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380509445155", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380675658651", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380951299232", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380951299232", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380675658651", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380935061405", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380971760140", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380933371444", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        result += PhoneContactImpl("380987820531", "Vasya", "http://www.youloveit.ru/uploads/posts/2017-05/1496158153_youloveit_ru_igra_test_kto_ty_iz_poni.jpg")
        return result
    }

    companion object {
        const val NUMBER_WITHOUT_CODE = 9
    }

}



