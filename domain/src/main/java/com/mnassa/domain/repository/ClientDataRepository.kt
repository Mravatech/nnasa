package com.mnassa.domain.repository

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 10/3/2018.
 */
interface ClientDataRepository {
    suspend fun getMinimumSupportedApiVersion(): ReceiveChannel<Int?>
}