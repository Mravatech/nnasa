package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 3/13/2018.
 */
interface Post : Model {
    val allConnections: Boolean
    val type: PostType
    val createdAt: Date
    val images: List<String>
    val locationPlace: LocationPlaceModel?
    val originalCreatedAt: Date
    val originalId: String
    val privacyConnections: List<String>
    val privacyType: PostPrivacyType
    val tags: List<String>
    val text: String?
    val updatedAt: Date
    val counters: PostCounters
    val author: ShortAccountModel
    val copyOwnerId: String?
    val price: Double
}

interface PostCounters : Serializable {
    val comments: Int
    val likes: Int
    val recommend: Int
    val reposts: Int
    val unreadResponse: Int
    val views: Int
}

sealed class PostType(val ordinal: Int) {
    object NEED: PostType(1)
    object OFFER: PostType(2)
    object GENERAL: PostType(3)
    object PROFILE: PostType(4)
}

sealed class PostPrivacyType {
    object PUBLIC: PostPrivacyType()
    object PRIVATE: PostPrivacyType()
}

sealed class ItemType {
    object EVENT: ItemType()
    object POST: ItemType()
}