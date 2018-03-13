package com.mnassa.data.network

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

    object ConnectionsStatus {
        const val CONNECTED = "connected"
        const val REQUESTED = "requested"
        const val SENT = "sended" //server side error
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
    }
}