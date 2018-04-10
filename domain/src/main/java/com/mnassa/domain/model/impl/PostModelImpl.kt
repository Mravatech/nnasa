package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by Peter on 3/15/2018.
 */
data class PostModelImpl(
        override var id: String,
        override val allConnections: Boolean,
        override val type: PostType,
        override val createdAt: Date,
        override val images: List<String>,
        override val locationPlace: LocationPlaceModel?,
        override val originalCreatedAt: Date,
        override val originalId: String,
        override val privacyConnections: List<String>,
        override val privacyType: PostPrivacyType,
        override val tags: List<String>,
        override val text: String?,
        override val updatedAt: Date,
        override val counters: PostCounters,
        override val author: ShortAccountModel,
        override val copyOwnerId: String?,
        override val price: Double,
        override val autoSuggest: PostAutoSuggest,
        override val repostAuthor: ShortAccountModel?
) : PostModel {
}

data class RecommendedProfilePostModelImpl(
        override var id: String,
        override val allConnections: Boolean,
        override val type: PostType,
        override val createdAt: Date,
        override val images: List<String>,
        override val locationPlace: LocationPlaceModel?,
        override val originalCreatedAt: Date,
        override val originalId: String,
        override val privacyConnections: List<String>,
        override val privacyType: PostPrivacyType,
        override val tags: List<String>,
        override val text: String?,
        override val updatedAt: Date,
        override val counters: PostCounters,
        override val author: ShortAccountModel,
        override val copyOwnerId: String?,
        override val price: Double,
        override val autoSuggest: PostAutoSuggest,
        override val repostAuthor: ShortAccountModel?,
        override val recommendedProfile: ShortAccountModel
) : RecommendedProfilePostModel {

}

data class PostCountersImpl(
        override val comments: Int,
        override val likes: Int,
        override val recommend: Int,
        override val reposts: Int,
        override val unreadResponse: Int,
        override val views: Int
) : PostCounters