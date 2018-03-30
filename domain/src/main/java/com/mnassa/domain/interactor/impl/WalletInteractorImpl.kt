package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.repository.WalletRepository

/**
 * Created by Peter on 3/30/2018.
 */
class WalletInteractorImpl(private val walletRepository: WalletRepository) : WalletInteractor, WalletRepository by walletRepository {
}