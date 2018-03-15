package com.mnassa.data.repository

/**
 * Created by Peter on 2/28/2018.
 */
internal object DatabaseContract {
    const val TABLE_ACCOUNTS = "accounts"
    const val TABLE_ACCOUNTS_COL_NUM_COMMUNITIES = "numberOfCommunities"
    const val TABLE_ACCOUNTS_COL_NUM_CONNECTIONS = "numberOfConnections"
    const val TABLE_ACCOUNTS_COL_NUM_DISCONNECTED = "numberOfDisconnected"
    const val TABLE_ACCOUNTS_COL_NUM_RECOMMENDATIONS = "numberOfRecommendations"
    const val TABLE_ACCOUNTS_COL_NUM_REQUESTED = "numberOfRequested"
    const val TABLE_ACCOUNTS_COL_NUM_SENT = "numberOfSent"
    const val TABLE_ACCOUNTS_COL_NUM_UNREAD_CHATS = "numberOfUnreadChats"
    const val TABLE_ACCOUNTS_COL_NUM_UNREAD_EVENTS = "numberOfUnreadEvents"
    const val TABLE_ACCOUNTS_COL_NUM_UNREAD_NEEDS = "numberOfUnreadNeeds"
    const val TABLE_ACCOUNTS_COL_NUM_UNREAD_NOTIFICATIONS = "numberOfUnreadNotifications"
    const val TABLE_ACCOUNTS_COL_NUM_UNREAD_RESPONSES = "numberOfUnreadResponses"
    //
    const val TABLE_ACCOUNT_LINKS = "accountLinks"
    //
    const val TABLE_CLIENT_DATA = "clientData"
    const val TABLE_CLIENT_DATA_COL_UI_VERSION = "mobileUiVersion"
    const val TABLE_CLIENT_DATA_COL_DISCONNECT_TIMEOUT = "parameters/disconnectTimeout"
    //
    const val TABLE_DICTIONARY = "dictionary"
    const val TABLE_DICTIONARY_COL_MOBILE_UI = "mobileUi"
    //
    const val TABLE_CONNECTIONS = "connections"

    const val TABLE_CONNECTIONS_COL_RECOMMENDED = "recommended"
    const val TABLE_CONNECTIONS_COL_REQUESTED = "requested"
    const val TABLE_CONNECTIONS_COL_CONNECTED = "connected"
    const val TABLE_CONNECTIONS_COL_SENT = "sent"
    const val TABLE_CONNECTIONS_COL_DISCONNECTED = "disconnected"
    const val TABLE_CONNECTIONS_COL_MUTED = "muted"
    const val TABLE_CONNECTIONS_COL_STATUSES = "statuses"
    //
    const val TABLE_CONNECTIONS_RECOMMENDED = "recommendedConnections"
    const val TABLE_CONNECTIONS_RECOMMENDED_COL_BY_PHONE = "byPhone"
    const val TABLE_CONNECTIONS_RECOMMENDED_COL_BY_GROUPS = "byGroups"
    const val TABLE_CONNECTIONS_RECOMMENDED_COL_BY_EVENTS = "byEvents"
    //
    const val TABLE_NEWS_FEED = "newsfeed"
    const val NEWS_FEED_TYPE_NEED = "need"
    const val NEWS_FEED_TYPE_ACCOUNT = "account"
    const val NEWS_FEED_TYPE_OFFER = "offer"
    const val NEWS_FEED_TYPE_GENERAL = "general"

    const val NEWS_FEED_PRIVACY_TYPE_PUBLIC = "public"
    const val NEWS_FEED_PRIVACY_TYPE_PRIVATE = "private"
    //
    const val TABLE_PUBLIC_ACCOUNTS = "publicAccounts"

}