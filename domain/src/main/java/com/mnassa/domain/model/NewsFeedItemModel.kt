package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 3/13/2018.
 */
interface NewsFeedItemModel : Model {
    val allConnections: Boolean
//    val adminPost: Boolean
    val type: NewsFeedItemType
    val createdAt: Date
    val images: List<String>
    val locationPlace: LocationPlaceModel?
//    val needPush: Boolean
    val originalCreatedAt: Date
    val originalId: String
    val privacyConnections: List<String>
    val privacyType: NewsFeedItemPrivacyType
    val tags: List<String>
    val text: String?
    val updatedAt: Date
    val counters: NewsFeedItemCounters
    val author: ShortAccountModel
    val copyOwnerId: String
}

interface NewsFeedItemCounters : Serializable {
    val comments: Int
    val likes: Int
    val recommend: Int
    val reposts: Int
    val unreadResponse: Int
    val views: Int
}

enum class NewsFeedItemType {
    NEED, OFFER, GENERAL, PROFILE
}

enum class NewsFeedItemPrivacyType {
    PUBLIC, PRIVATE
}