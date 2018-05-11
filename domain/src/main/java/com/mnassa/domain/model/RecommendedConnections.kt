package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 11.03.2018.
 */
interface RecommendedConnections : Serializable {
    val recommendations: Map<String, List<ShortAccountModel>>

    val isEmpty: Boolean get() = recommendations.isEmpty()
}