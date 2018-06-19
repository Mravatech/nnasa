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
    const val TABLE_ACCOUNTS_COL_POINTS = "points"
    const val TABLE_ACCOUNTS_COL_TOTAL_INCOME = "totalIncome"
    const val TABLE_ACCOUNTS_COL_TOTAL_OUTCOME = "totalOutcome"
    const val TABLE_ACCOUNTS_COL_INVITES_COUNT = "invites"
    const val TABLE_ACCOUNTS_COL_PERMISSIONS = "permissions"
    const val TABLE_ACCOUNTS_COL_STATE = "state"
    const val TABLE_ACCOUNTS_COL_STATE_DISABLED = "disabled"
    const val TABLE_ACCOUNTS_COL_TAG_REMINDER_TIME = "tagReminderShowedAt"
    //
    const val TABLE_ACCOUNT_LINKS = "accountLinks"
    //
    const val TABLE_CLIENT_DATA = "clientData"
    const val TABLE_CLIENT_DATA_COL_UI_VERSION = "mobileUiVersion"
    const val TABLE_CLIENT_DATA_COL_DISCONNECT_TIMEOUT = "parameters/disconnectTimeout"
    const val TABLE_CLIENT_DATA_COL_DEFAULT_EXPIRATION_TIME = "parameters/defaultExpirationTime"
    const val TABLE_CLIENT_DATA_PUSH_TYPES = "pushTypes"
    const val TABLE_CLIENT_DATA_COL_MAINTENANCE = "maintenance"
    const val TABLE_CLIENT_DATA_COL_TAGS_UPDATE_PERIOD = "$TABLE_CLIENT_DATA/parameters/tagsReminder"
    //
    const val TABLE_DICTIONARY = "dictionary"
    const val TABLE_DICTIONARY_COL_MOBILE_UI = "mobileUi"
    const val TABLE_DICTIONARY_COL_PAYMENT_TYPES_PROMOTE_EVENT = "paymentTypes/promoteEvent"
    const val TABLE_DICTIONARY_COL_PAYMENT_TYPES_PROMOTE_POST = "paymentTypes/promotePost"
    const val TABLE_DICTIONARY_COL_REWARD_FOR_COMMENT ="paymentTypes/rewardForComment"
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
    const val TABLE_CONNECTIONS_RECOMMENDED_COL_BY_GROUPS = "byRefer" //byRefer
    const val TABLE_CONNECTIONS_RECOMMENDED_COL_BY_EVENTS = "byEvents"//"byEvents" //
    //
    const val TABLE_POSTS = "posts"
    const val TABLE_PUBLIC_POSTS = "publicPosts"
    //
    const val TABLE_NOTIFICATIONS = "notifications"
    const val TABLE_NOTIFICATIONS_OLD = "notificationsOld"
    //
    const val TABLE_CHAT = "chats"
    const val TABLE_CHAT_LIST = "chatsList"
    const val TABLE_CHAT_MESSAGES = "chatsMessages"
    const val TABLE_CHAT_TYPE_PRIVATE = "private"
    //
    const val TABLE_INVITETION = "invitations"
    //
    const val ACCOUNTS_PUSH_SETTINGS = "accountsPushSettings"
    //
    const val TABLE_NEWS_FEED = "newsfeed"
    const val NEWS_FEED_TYPE_NEED = "need"
    const val NEWS_FEED_TYPE_ACCOUNT = "account"
    const val NEWS_FEED_TYPE_OFFER = "offer"
    const val NEWS_FEED_TYPE_GENERAL = "general"
    const val NEWS_FEED_TYPE_INFO = "info"

    const val NEWS_FEED_PRIVACY_TYPE_PUBLIC = "public"
    const val NEWS_FEED_PRIVACY_TYPE_PRIVATE = "private"
    const val NEWS_FEED_PRIVACY_TYPE_WORLD = "world"
    //
    const val TABLE_PUBLIC_ACCOUNTS = "publicAccounts"
    //
    const val TABLE_TAGS = "tags"
    //
    const val TABLE_COMMENTS = "comments"
    //
    const val TABLE_COMMENT_REPLIES = "replyComments"
    //
    const val TABLE_TRANSACTIONS = "transactions"
    //
    const val COMPLAINT_REASON = "complaintReason"
    //
    const val TABLE_EVENTS = "events"
    const val TABLE_EVENTS_COLLECTION_FEED = "feed"
    //
    const val TABLE_ALL_EVENTS = "allEvents"
    //
    const val TABLE_EVENT_TICKETS = "eventTicketsData"
    //
    const val TABLE_EVENT_ATTENDIES = "attendedUsers"
    const val TABLE_EVENT_ATTENDIES_COLLECTION = "attendanceList"
    //
    const val TABLE_INFO_FEED = "infofeed"
    //
    const val TABLE_OFFER_CATEGORY = "categories"
    //offer
    const val SHARE_OFFER_POST = "dictionary/paymentTypes/shareOfferPost"
    const val SHARE_OFFER_POST_PER_USER = "dictionary/paymentTypes/shareOfferPostPerUser"
    const val PROMOTE_POST = "dictionary/paymentTypes/promotePost"
    const val PROMOTE_EVENT = "dictionary/paymentTypes/promoteEvent"
    //users
    const val TABLE_USERS = "users"
    const val TABLE_USERS_COL_STATE = "state"
    const val TABLE_USERS_COL_STATE_DISABLED = "disabled"

    //
    const val EXPIRATION_TYPE_ACTIVE = "active"
    const val EXPIRATION_TYPE_EXPIRED = "expired"
    const val EXPIRATION_TYPE_CLOSED = "closed"
    const val EXPIRATION_TYPE_FULFILLED = "fulfilled"

    //communities
    const val TABLE_GROUPS = "communities"
    const val TABLE_GROUPS_COL_MY = "myCommunities"
    const val TABLE_GROUPS_COL_INVITES = "invitesToCommunities"
    const val TABLE_GROUPS_ALL = "allCommunities"
    const val TABLE_GROUPS_ALL_COL_FEED = "postFeed"
    const val TABLE_GROUPS_ALL_COL_MEMBERS = "members"
    const val TABLE_GROUPS_ALL_COL_INVITES = "invites"
    const val TABLE_GROUPS_ALL_COL_WALLET = "wallet"

}