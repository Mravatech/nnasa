package com.mnassa.domain.repository

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 3/7/2018.
 */
interface CountersRepository {
    suspend fun produceNumberOfCommunities(): ReceiveChannel<Int>
    suspend fun produceNumberOfConnections(): ReceiveChannel<Int>
    suspend fun produceNumberOfDisconnected(): ReceiveChannel<Int>
    suspend fun produceNumberOfRecommendations(): ReceiveChannel<Int>
    suspend fun produceNumberOfRequested(): ReceiveChannel<Int>
    suspend fun produceNumberOfSent(): ReceiveChannel<Int>
    suspend fun produceNumberOfUnreadChats(): ReceiveChannel<Int>
    suspend fun produceNumberOfUnreadEvents(): ReceiveChannel<Int>
    suspend fun produceNumberOfUnreadNeeds(): ReceiveChannel<Int>
    suspend fun produceNumberOfUnreadNotifications(): ReceiveChannel<Int>
    suspend fun produceNumberOfUnreadResponses(): ReceiveChannel<Int>
}