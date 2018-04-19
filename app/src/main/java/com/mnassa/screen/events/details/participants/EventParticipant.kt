package com.mnassa.screen.events.details.participants

import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 4/19/2018.
 */
data class EventParticipant(val user: ShortAccountModel, val isInConnections: Boolean, val guestsCount: Int)