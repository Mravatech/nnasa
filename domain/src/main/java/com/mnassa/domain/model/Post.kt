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
    val autoSuggest: PostAutoSuggest
}

interface PostCounters : Serializable {
    val comments: Int
    val likes: Int
    val recommend: Int
    val reposts: Int
    val unreadResponse: Int
    val views: Int
}

interface PostAutoSuggest: Serializable {
    val total: Int
    val youCanHelp: Boolean
    val aids: List<String>

    companion object {
        val EMPTY = object: PostAutoSuggest {
            override val total: Int = 0
            override val youCanHelp: Boolean = false
            override val aids: List<String> = emptyList()
        }
    }
}

sealed class PostType(val ordinal: Int)  : Serializable {
    object NEED: PostType(1)
    object OFFER: PostType(2)
    object GENERAL: PostType(3)
    object PROFILE: PostType(4)
}

sealed class PostPrivacyType : Serializable {
    object PUBLIC: PostPrivacyType()
    object PRIVATE: PostPrivacyType()
    object WORLD: PostPrivacyType()
}

sealed class ItemType : Serializable {
    object EVENT: ItemType()
    object POST: ItemType()
}