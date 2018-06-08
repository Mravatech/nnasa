package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.bean.firebase.InvitationDbEntity
import com.mnassa.data.network.bean.retrofit.request.ContactsRequest
import com.mnassa.data.network.bean.retrofit.request.PhoneContactRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.domain.repository.InviteRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteRepositoryImpl(
        private val inviteApi: FirebaseInviteApi,
        private val exceptionHandler: ExceptionHandler,
        private val databaseReference: DatabaseReference,
        private val converter: ConvertersContext,
        private val db: DatabaseReference,
        private val userRepository: UserRepository
) : InviteRepository {

    override suspend fun inviteContact(phoneContact: PhoneContact) {
        inviteApi.inviteContact(ContactsRequest(phoneContact.toInviteRequest())).handleException(exceptionHandler)
    }

    override suspend fun inviteContactToGroup(phoneContact: PhoneContact, communityId: String) {
        val request = phoneContact.toInviteRequest().copy(
                type = NetworkContract.InviteType.COMMUNITY,
                id = communityId
        )
        inviteApi.inviteContact(ContactsRequest(request)).handleException(exceptionHandler)
    }

    override suspend fun inviteContactToPost(phoneContact: PhoneContact, postId: String) {
        val request = phoneContact.toInviteRequest().copy(
                type = NetworkContract.InviteType.POST,
                id = postId
        )
        inviteApi.inviteContact(ContactsRequest(request)).handleException(exceptionHandler)
    }

    override suspend fun inviteContactToEvent(phoneContact: PhoneContact, eventId: String) {
        val request = phoneContact.toInviteRequest().copy(
                type = NetworkContract.InviteType.EVENT,
                id = eventId
        )
        inviteApi.inviteContact(ContactsRequest(request)).handleException(exceptionHandler)
    }

    override suspend fun getInvitedContacts(userId: String): List<PhoneContactInvited> {
        val data = async {
            val invited = databaseReference
                    .child(DatabaseContract.TABLE_INVITETION)
                    .child(userId)
                    .awaitList<InvitationDbEntity>(exceptionHandler)
            converter.convertCollection(invited, PhoneContactInvited::class.java)
        }.await().toMutableList()
        return data.sortedWith(compareBy(PhoneContactInvited::createdAt))
    }

    override suspend fun getInvitesCountChannel(): ReceiveChannel<Int> {
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountIdOrException())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_INVITES_COUNT)
                .toValueChannel<Int>(exceptionHandler).map { it ?: 0 }
    }

    private fun PhoneContact.toInviteRequest(): PhoneContactRequest {
        val description = if (fullName.isEmpty()) null else fullName
        return PhoneContactRequest(phoneNumber, description, avatar, type = null, id = null)
    }
}
