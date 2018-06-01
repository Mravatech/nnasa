package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 3/13/2018.
 */
interface PostModel : Model {
    val allConnections: Boolean
    val type: PostType
    val createdAt: Date
    val attachments: List<PostAttachment>
    val locationPlace: LocationPlaceModel?
    val originalCreatedAt: Date
    val originalId: String
    val privacyConnections: Set<String>
    val privacyType: PostPrivacyType
    val tags: List<String>
    val text: String?
    val statusOfExpiration: ExpirationType?
    var timeOfExpiration: Date?
    val updatedAt: Date
    val counters: PostCounters
    val author: ShortAccountModel
    val copyOwnerId: String?
    val price: Double
    val autoSuggest: PostAutoSuggest
    val repostAuthor: ShortAccountModel?
}

interface InfoPostModel: PostModel {
    val title: String
    var isPinned: Boolean
}

interface OfferPostModel: PostModel {
    val title: String
    val category: String?
    val subCategory: String?
}

sealed class PostAttachment : Serializable {
    data class PostPhotoAttachment(val photoUrl: String) : PostAttachment()
    data class PostVideoAttachment(val previewUrl: String, val videoUrl: String) : PostAttachment()
}

interface RecommendedProfilePostModel : PostModel {
    val recommendedProfile: ShortAccountModel?
    var offers: List<TagModel>
}

interface PostCounters : Serializable {
    val comments: Int
    val likes: Int
    val recommend: Int
    val reposts: Int
    val unreadResponse: Int
    val views: Int
}

interface PostAutoSuggest : Serializable {
    val total: Int
    val youCanHelp: Boolean
    val accountIds: List<String>

    companion object {
        val EMPTY = object : PostAutoSuggest {
            override val total: Int = 0
            override val youCanHelp: Boolean = false
            override val accountIds: List<String> = emptyList()
        }
    }
}

sealed class PostType(val ordinal: Int) : Serializable {
    class NEED : PostType(1)
    class OFFER : PostType(2)
    class GENERAL : PostType(3)
    class PROFILE : PostType(4)
    class INFO : PostType(5)
    class OTHER : PostType(6)
}

sealed class PostPrivacyType : Serializable {
    class PUBLIC : PostPrivacyType()
    class PRIVATE : PostPrivacyType()
    class WORLD : PostPrivacyType()
}

sealed class EntityType : Serializable {
    class EVENT : EntityType()
    class POST : EntityType()
}

sealed class ExpirationType(val text: String) : Serializable {
    class ACTIVE(text: String) : ExpirationType(text)
    class EXPIRED(text: String) : ExpirationType(text)
    class CLOSED(text: String) : ExpirationType(text)
    class FULFILLED(text: String) : ExpirationType(text)
}
