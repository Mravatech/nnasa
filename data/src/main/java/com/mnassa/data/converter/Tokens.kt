package com.mnassa.data.converter

import com.mnassa.domain.model.GroupModel

/**
 * Created by Peter on 5/22/2018.
 */
data class PostAdditionInfo(
        val groupIds: Set<String> = emptySet()
) {
    companion object {
        fun withGroup(groupId: String?): PostAdditionInfo {
            return PostAdditionInfo(groupId?.let { setOf(it) } ?: emptySet())
        }

        fun withGroup(groupIds: Iterable<String>): PostAdditionInfo {
            return PostAdditionInfo(groupIds.toSet())
        }
    }
}

data class EventAdditionInfo(
        val groupIds: List<GroupModel>
)