package com.mnassa.screen.events.details.participants

import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 4/19/2018.
 */
sealed class EventParticipantItem {
    data class User(val user: ShortAccountModel, val isInConnections: Boolean, val guestsCount: Int) : EventParticipantItem()
    object ConnectionsHeader : EventParticipantItem()
    object OtherHeader : EventParticipantItem()
}
