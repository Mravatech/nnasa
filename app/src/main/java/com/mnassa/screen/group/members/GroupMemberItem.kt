package com.mnassa.screen.group.members

import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 5/25/2018.
 */
data class GroupMemberItem(val user: ShortAccountModel, val isAdmin: Boolean)