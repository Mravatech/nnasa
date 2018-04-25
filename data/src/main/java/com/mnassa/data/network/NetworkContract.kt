package com.mnassa.data.network

import com.mnassa.domain.model.PostPrivacyType

/**
 * Created by Peter on 2/28/2018.
 */
object NetworkContract {

    object Base {
        const val LANGUAGE_HEADER = "language"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val AUTHORIZATION_HEADER_VALUE_MASK = "Bearer %s"
        const val ACCOUNT_ID_HEADER = "aid"
    }

    object AccountType {
        const val PERSONAL = "personal"
        const val ORGANIZATION = "organization"
    }

    object ItemType {
        const val ORIGINAL = "original"
        const val REPOST = "repost"
    }

    object EntityType {
        const val POST = "post"
        const val EVENT = "event"
    }

    object PostType {
        const val NEED = "need"
        const val GENERAL = "general"
        const val OFFER = "offer"
        const val ACCOUNT = "account"
    }

    object PostPrivacyType {
        const val PUBLIC = "public"
        const val PRIVATE = "private"
        const val WORLD = "world"
    }

    object ConnectionsStatus {
        const val CONNECTED = "connected"
        const val REQUESTED = "requested"
        const val SENT = "sent"
        const val DISCONNECTED = "disconnected"
        const val RECOMMENDED = "recommended"
    }

    object ConnectionAction {
        const val CONNECT = "connect"
        const val ACCEPT = "accept"
        const val DECLINE = "decline"
        const val DISCONNECT = "disconnect"
        const val MUTE = "mute"
        const val UN_MUTE = "unmute"
        const val REVOKE = "revoke"
    }

    object ResponseCode {
        const val UNAUTHORIZED = 403
        const val NO_RIGHTS_TO_COMMENT = 400
    }

    object ErrorCode {
        const val NO_RIGHTS_TO_COMMENT = "noRightToCommentPost"
    }

    object Gender {
        const val MALE = "male"
        const val FEMALE = "female"
    }

    object Complaint {
        const val ACCOUNT_TYPE = "account"
        const val POST_TYPE = "post"
    }

    object EventStatus {
        const val ANNULED = "annulled"
        const val OPENED = "opened"
        const val CLOSED = "closed"
        const val SUSPENDED = "suspended"
    }

    object EventDuration {
        const val MINUTE = "minute"
        const val HOUR = "hour"
        const val DAY = "day"
    }

    object EventLocationType {
        const val SPECIFY = "specifyLocation"
        const val NOT_DEFINED = "notDefined"
        const val LATER = "willBeAnnouncedLater"
    }

    object EventType {
        const val LECTURE = "lecture"
        const val DISCUSSION = "discussion"
        const val WORKSHOP = "workshop"
        const val EXERCISE = "exercise"
        const val ACTIVITY = "activity"
    }
}

val PostPrivacyType.stringValue: String
    get() = when (this) {
        is PostPrivacyType.PUBLIC -> NetworkContract.PostPrivacyType.PUBLIC
        is PostPrivacyType.PRIVATE -> NetworkContract.PostPrivacyType.PRIVATE
        is PostPrivacyType.WORLD -> NetworkContract.PostPrivacyType.WORLD
    }