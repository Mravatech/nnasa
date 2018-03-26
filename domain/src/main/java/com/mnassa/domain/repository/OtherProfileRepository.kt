package com.mnassa.domain.repository

import com.mnassa.domain.model.ProfileAccountModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

interface OtherProfileRepository {

    suspend fun getPrifileByAccountId(accountId: String) : ProfileAccountModel?

}