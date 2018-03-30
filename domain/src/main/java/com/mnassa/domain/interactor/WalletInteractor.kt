package com.mnassa.domain.interactor

import com.mnassa.domain.repository.WalletRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/30/2018.
 */
interface WalletInteractor : WalletRepository {
    override suspend fun getBalance(): ReceiveChannel<Long>
}