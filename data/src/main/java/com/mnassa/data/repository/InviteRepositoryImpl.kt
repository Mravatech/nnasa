package com.mnassa.data.repository

import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.bean.retrofit.request.SendContactsRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.InviteRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/5/2018.
 */
class InviteRepositoryImpl(private val api: FirebaseInviteApi, private val exceptionHandler: ExceptionHandler) : InviteRepository {

    override suspend fun sendContacts(phoneNumbers: List<String>) {
        api.sendContacts(SendContactsRequest(phoneNumbers)).handleException(exceptionHandler)
    }

    override fun getRecommendedUsers(): ReceiveChannel<List<ShortAccountModel>> {
        TODO()
    }

    override suspend fun connect(userAccountIds: List<String>) {

    }
}