package com.mnassa.screen.connections

import com.mnassa.domain.model.ShortAccountModel

/**
 * @author Artem Chepurnoy
 */
data class DeclineConnection(
    val account: ShortAccountModel,
    val disconnectTimeoutDays: Int
)
