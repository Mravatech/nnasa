package com.mnassa.screen.profile.model

import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

data class ProfileModel(
        val profile: ProfileAccountModel,
        val interests: List<TagModel>,
        val offers: List<TagModel>,
        val isMyProfile: Boolean,
        val connectionStatus: ConnectionStatus
) {
    fun getAccountType(): Accounts {
        return if (isMyProfile && profile.accountType == AccountType.ORGANIZATION) {
            Accounts.MY_COMPANY
        } else if (isMyProfile && profile.accountType == AccountType.PERSONAL) {
            Accounts.MY_PERSONAL
        }else if (profile.accountType == AccountType.ORGANIZATION) {
            Accounts.USER_COMPANY
        }else if (profile.accountType == AccountType.PERSONAL) {
            Accounts.USER_PERSONAL
        }else{
            throw IllegalArgumentException("Wrong ProfileModel")
        }
    }
}

enum class Accounts {
    MY_PERSONAL, MY_COMPANY, USER_PERSONAL, USER_COMPANY
}

