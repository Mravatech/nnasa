package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 5/14/2018.
 */
interface GroupModel : Model {
    val name: String
    val type: GroupType
    val creator: String
    val avatar: String?
}

sealed class GroupType : Serializable {
    class Public : GroupType()
    class Private : GroupType()
}