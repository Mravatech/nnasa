package com.mnassa.domain.repository

import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/7/2018.
 */
interface CountersRepository {
    val numberOfCommunities: ReceiveChannel<Int>
    val numberOfConnections: ReceiveChannel<Int>
    val numberOfDisconnected: ReceiveChannel<Int>
    val numberOfRecommendations: ReceiveChannel<Int>
    val numberOfRequested: ReceiveChannel<Int>
    val numberOfSent: ReceiveChannel<Int>
    val numberOfUnreadChats: ReceiveChannel<Int>
    val numberOfUnreadEvents: ReceiveChannel<Int>
    val numberOfUnreadNeeds: ReceiveChannel<Int>
    val numberOfUnreadNotifications: ReceiveChannel<Int>
    val numberOfUnreadResponses: ReceiveChannel<Int>

}