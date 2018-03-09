package com.mnassa.domain.model

/**
 * Created by Peter on 11.03.2018.
 */
interface RecommendedConnections {
    val byPhone: Map<String, List<ShortAccountModel>>
    val byGroups: Map<String, List<ShortAccountModel>>
    val byEvents: Map<String, List<ShortAccountModel>>

    val isEmpty: Boolean get() = byPhone.isEmpty() && byGroups.isEmpty() && byEvents.isEmpty()
}