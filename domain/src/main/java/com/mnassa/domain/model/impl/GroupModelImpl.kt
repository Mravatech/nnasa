package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*

/**
 * Created by Peter on 5/21/2018.
 */
data class GroupModelImpl(
        override var id: String,
        override val name: String,
        override val description: String,
        override val type: GroupType = GroupType.Private(),
        override val avatar: String?,
        override val admins: List<String> = emptyList(),
        override val numberOfParticipants: Long = 0L,
        override val numberOfInvites: Long = 0L,
        override val creator: ShortAccountModel? = null,
        override val website: String? = null,
        override val locationPlace: LocationPlaceModel? = null,
        override val tags: List<String> = emptyList(),
        override val permissions: GroupPermissions
) : GroupModel