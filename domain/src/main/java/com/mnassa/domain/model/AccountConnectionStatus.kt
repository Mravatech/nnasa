package com.mnassa.domain.model

/**
 * Created by Peter on 3/5/2018.
 */
interface AccountConnectionStatus {
    var connectionStatus: ConnectionStatus
}

enum class ConnectionStatus {
    CONNECTED, REQUESTED, SENT, DISCONNECTED, RECOMMENDED
}

enum class ConnectionAction {
    CONNECT, ACCEPT, DECLINE, DISCONNECT, MUTE, UN_MUTE, REVOKE
}