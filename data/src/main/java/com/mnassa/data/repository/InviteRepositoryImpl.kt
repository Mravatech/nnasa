package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.bean.firebase.InvitationDbEntity
import com.mnassa.data.network.bean.retrofit.request.ContactsRequest
import com.mnassa.data.network.bean.retrofit.request.PhoneContactRequest
import com.mnassa.data.network.bean.retrofit.response.MnassaResponse
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.domain.repository.InviteRepository
import kotlinx.coroutines.experimental.async
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteRepositoryImpl(
        private val inviteApi: FirebaseInviteApi,
        private val exceptionHandler: ExceptionHandler,
        private val databaseReference: DatabaseReference,
        private val converter: ConvertersContext
) : InviteRepository {

    override suspend fun inviteContact(phoneContact: PhoneContact) {
        val description = if (phoneContact.fullName.isEmpty()) null else phoneContact.fullName
        val phones = listOf(PhoneContactRequest(phoneContact.phoneNumber, description, phoneContact.avatar))
        val response: MnassaResponse = inviteApi.inviteContact(ContactsRequest(phones)).handleException(exceptionHandler)
        Timber.i(response.status)
    }

    override suspend fun getInvitedContacts(userId: String): List<PhoneContactInvited> {
        return async {
            val invited = databaseReference
                    .child(DatabaseContract.TABLE_INVITETION)
                    .child(userId)
                    .awaitList<InvitationDbEntity>(exceptionHandler)
            converter.convertCollection(invited, PhoneContactInvited::class.java)
        }.await()

    }
}
