package com.mnassa.screen.events.details.participants

import com.mnassa.domain.model.ShortAccountModel
import java.io.Serializable

/**
 * Created by Peter on 4/19/2018.
 */
sealed class EventParticipantItem : Comparable<EventParticipantItem>, Serializable {

    data class User(val user: ShortAccountModel, val isInConnections: Boolean, val guestsCount: Int, var isChecked: Boolean = false) : EventParticipantItem() {
        override val order: String get() = (if (isInConnections) ORDER_CONNECTIONS_HEADER else ORDER_OTHER_HEADER) + user.id
    }

    data class Guest(val parent: User, val position: Int, var isChecked: Boolean = false) : EventParticipantItem() {
        override val order: String get() = parent.order + "_" + position
    }

    data class ConnectionsHeader(val canEdit: Boolean) : EventParticipantItem() {
        override val order: String = ORDER_CONNECTIONS_HEADER
    }

    object OtherHeader : EventParticipantItem() {
        override val order: String = ORDER_OTHER_HEADER
    }

    abstract val order: String

    override fun compareTo(other: EventParticipantItem): Int {
        return this.order.compareTo(other.order)
    }

    private companion object {
        const val ORDER_CONNECTIONS_HEADER = "1_"
        const val ORDER_OTHER_HEADER = "2_"
    }
}

fun EventParticipantItem.withGuests(): List<EventParticipantItem> {
    return if (this is EventParticipantItem.User && guestsCount > 0) {
        val result = ArrayList<EventParticipantItem>()
        result += this
        for (i in 0 until guestsCount) result += EventParticipantItem.Guest(this, i + 1)
        result
    } else {
        listOf(this)
    }
}
