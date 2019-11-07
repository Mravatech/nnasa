package com.mnassa.domain.model

/**
 * Created by Peter on 3/5/2018.
 */

enum class ConnectionStatus {
    CONNECTED, REQUESTED, SENT,
    DISCONNECTED, RECOMMENDED, VALUE_CENTER,
    NONE
}

val ConnectionStatus.canBeConnected: Boolean
    get() =
        this == ConnectionStatus.REQUESTED || this ==
                ConnectionStatus.DISCONNECTED || this ==
                ConnectionStatus.RECOMMENDED

enum class ConnectionAction {
    CONNECT, ACCEPT, DECLINE,
    DISCONNECT, MUTE, UN_MUTE,
    REVOKE
}