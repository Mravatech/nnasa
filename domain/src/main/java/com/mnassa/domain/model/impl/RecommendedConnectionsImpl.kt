package com.mnassa.domain.model.impl

import com.mnassa.domain.model.RecommendedConnections
import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 9.03.2018.
 */
data class RecommendedConnectionsImpl(
        override val recommendations: Map<String, List<ShortAccountModel>>
) : RecommendedConnections