package com.mnassa.screen.group.invite

import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 7/23/2018.
 */
data class UserInvite(
        val user: ShortAccountModel,
        val isInvited: Boolean,
        val isMember: Boolean
)