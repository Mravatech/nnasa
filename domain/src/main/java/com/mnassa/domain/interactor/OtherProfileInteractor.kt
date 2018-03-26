package com.mnassa.domain.interactor

import com.mnassa.domain.model.ProfileAccountModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

interface OtherProfileInteractor {

    suspend fun getPrifileByAccountId(accountId: String): ProfileAccountModel?

}