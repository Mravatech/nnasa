package com.mnassa.domain.repository

import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 10/3/2018.
 */
interface ClientDataRepository {
    fun getMinimumSupportedApiVersion(): ReceiveChannel<Int?>
}