package com.mnassa.data.repository

import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.repository.DatabaseContract.MINIMUM_SUPPORTED_API_VERSION
import com.mnassa.domain.repository.ClientDataRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 10/3/2018.
 */
class ClientDataRepositoryImpl(private val db: DatabaseReference, private val exceptionHandler: ExceptionHandler) : ClientDataRepository {

    override fun getMinimumSupportedApiVersion(): ReceiveChannel<Int?> {
        return db.child(MINIMUM_SUPPORTED_API_VERSION)
                .toValueChannel(exceptionHandler)
    }
}