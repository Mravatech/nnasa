package com.mnassa.domain.model.impl

import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.GroupType
import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 5/21/2018.
 */
data class GroupModelImpl(
        override var id: String,
        override val name: String,
        override val description: String,
        override val type: GroupType,
        override val avatar: String?,
        override val isAdmin: Boolean,
        override val admins: List<String>,
        override val numberOfParticipants: Long,
        override val creator: ShortAccountModel

) : GroupModel {
}