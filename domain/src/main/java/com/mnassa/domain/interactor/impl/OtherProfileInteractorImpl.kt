package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.OtherProfileInteractor
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.repository.OtherProfileRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */
class OtherProfileInteractorImpl(private val otherProfileRepository: OtherProfileRepository) : OtherProfileInteractor {
    override suspend fun getPrifileByAccountId(accountId: String): ProfileAccountModel? {
        return otherProfileRepository.getPrifileByAccountId(accountId)
    }
}