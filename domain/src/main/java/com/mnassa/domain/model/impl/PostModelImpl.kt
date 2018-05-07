package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by Peter on 3/15/2018.
 */
open class PostModelImpl(
        override var id: String,
        override val allConnections: Boolean,
        override val type: PostType,
        override val createdAt: Date,
        override val images: List<String>,
        override val locationPlace: LocationPlaceModel?,
        override val originalCreatedAt: Date,
        override val originalId: String,
        override val privacyConnections: Set<String>,
        override val privacyType: PostPrivacyType,
        override val tags: List<String>,
        override val text: String?,
        override val statusOfExpiration: ExpirationType,
        override val timeOfExpiration: Date?,
        override val updatedAt: Date,
        override val counters: PostCounters,
        override val author: ShortAccountModel,
        override val copyOwnerId: String?,
        override val price: Double,
        override val autoSuggest: PostAutoSuggest,
        override val repostAuthor: ShortAccountModel?
) : PostModel {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostModelImpl

        if (id != other.id) return false
        if (allConnections != other.allConnections) return false
        if (type != other.type) return false
        if (createdAt != other.createdAt) return false
        if (images != other.images) return false
        if (locationPlace != other.locationPlace) return false
        if (originalCreatedAt != other.originalCreatedAt) return false
        if (originalId != other.originalId) return false
        if (privacyConnections != other.privacyConnections) return false
        if (privacyType != other.privacyType) return false
        if (tags != other.tags) return false
        if (text != other.text) return false
        if (updatedAt != other.updatedAt) return false
        if (counters != other.counters) return false
        if (author != other.author) return false
        if (copyOwnerId != other.copyOwnerId) return false
        if (price != other.price) return false
        if (autoSuggest != other.autoSuggest) return false
        if (repostAuthor != other.repostAuthor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + allConnections.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + images.hashCode()
        result = 31 * result + (locationPlace?.hashCode() ?: 0)
        result = 31 * result + originalCreatedAt.hashCode()
        result = 31 * result + originalId.hashCode()
        result = 31 * result + privacyConnections.hashCode()
        result = 31 * result + privacyType.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + counters.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + (copyOwnerId?.hashCode() ?: 0)
        result = 31 * result + price.hashCode()
        result = 31 * result + autoSuggest.hashCode()
        result = 31 * result + (repostAuthor?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "PostModelImpl(id='$id', allConnections=$allConnections, type=$type, createdAt=$createdAt, images=$images, locationPlace=$locationPlace, originalCreatedAt=$originalCreatedAt, originalId='$originalId', privacyConnections=$privacyConnections, privacyType=$privacyType, tags=$tags, text=$text, updatedAt=$updatedAt, counters=$counters, author=$author, copyOwnerId=$copyOwnerId, price=$price, autoSuggest=$autoSuggest, repostAuthor=$repostAuthor)"
    }


}

class RecommendedProfilePostModelImpl(
        override var id: String,
        override val allConnections: Boolean,
        override val type: PostType,
        override val createdAt: Date,
        override val images: List<String>,
        override val locationPlace: LocationPlaceModel?,
        override val originalCreatedAt: Date,
        override val originalId: String,
        override val privacyConnections: Set<String>,
        override val privacyType: PostPrivacyType,
        override val tags: List<String>,
        override val text: String?,
        override val statusOfExpiration: ExpirationType,
        override val timeOfExpiration: Date?,
        override val updatedAt: Date,
        override val counters: PostCounters,
        override val author: ShortAccountModel,
        override val copyOwnerId: String?,
        override val price: Double,
        override val autoSuggest: PostAutoSuggest,
        override val repostAuthor: ShortAccountModel?,
        override val recommendedProfile: ShortAccountModel,
        override var offers: List<TagModel>
) : PostModelImpl(
        id,
        allConnections,
        type,
        createdAt,
        images,
        locationPlace,
        originalCreatedAt,
        originalId,
        privacyConnections,
        privacyType,
        tags,
        text,
        statusOfExpiration,
        timeOfExpiration,
        updatedAt,
        counters,
        author,
        copyOwnerId,
        price,
        autoSuggest,
        repostAuthor
), RecommendedProfilePostModel {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as RecommendedProfilePostModelImpl

        if (id != other.id) return false
        if (allConnections != other.allConnections) return false
        if (type != other.type) return false
        if (createdAt != other.createdAt) return false
        if (images != other.images) return false
        if (locationPlace != other.locationPlace) return false
        if (originalCreatedAt != other.originalCreatedAt) return false
        if (originalId != other.originalId) return false
        if (privacyConnections != other.privacyConnections) return false
        if (privacyType != other.privacyType) return false
        if (tags != other.tags) return false
        if (text != other.text) return false
        if (updatedAt != other.updatedAt) return false
        if (counters != other.counters) return false
        if (author != other.author) return false
        if (copyOwnerId != other.copyOwnerId) return false
        if (price != other.price) return false
        if (autoSuggest != other.autoSuggest) return false
        if (repostAuthor != other.repostAuthor) return false
        if (recommendedProfile != other.recommendedProfile) return false
        if (offers != other.offers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + allConnections.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + images.hashCode()
        result = 31 * result + (locationPlace?.hashCode() ?: 0)
        result = 31 * result + originalCreatedAt.hashCode()
        result = 31 * result + originalId.hashCode()
        result = 31 * result + privacyConnections.hashCode()
        result = 31 * result + privacyType.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + counters.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + (copyOwnerId?.hashCode() ?: 0)
        result = 31 * result + price.hashCode()
        result = 31 * result + autoSuggest.hashCode()
        result = 31 * result + (repostAuthor?.hashCode() ?: 0)
        result = 31 * result + recommendedProfile.hashCode()
        result = 31 * result + offers.hashCode()
        return result
    }

    override fun toString(): String {
        return "RecommendedProfilePostModelImpl(id='$id', allConnections=$allConnections, type=$type, createdAt=$createdAt, images=$images, locationPlace=$locationPlace, originalCreatedAt=$originalCreatedAt, originalId='$originalId', privacyConnections=$privacyConnections, privacyType=$privacyType, tags=$tags, text=$text, updatedAt=$updatedAt, counters=$counters, author=$author, copyOwnerId=$copyOwnerId, price=$price, autoSuggest=$autoSuggest, repostAuthor=$repostAuthor, recommendedProfile=$recommendedProfile, offers=$offers)"
    }

}

data class PostCountersImpl(
        override val comments: Int,
        override val likes: Int,
        override val recommend: Int,
        override val reposts: Int,
        override val unreadResponse: Int,
        override val views: Int
) : PostCounters