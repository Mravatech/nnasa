package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupModel : Model {
    val name: String
    val description: String
    val type: GroupType
    val avatar: String?
    val creator: ShortAccountModel
    val isAdmin: Boolean
    val admins: List<String>
    val numberOfParticipants: Long

}

sealed class GroupType : Serializable {
    class Public : GroupType()
    class Private : GroupType()
}