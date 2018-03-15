package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by Peter on 3/15/2018.
 */
data class NewsFeedItemModelImpl(
        override var id: String,
        override val allConnections: Boolean,
        override val type: NewsFeedItemType,
        override val createdAt: Date,
        override val images: List<String>,
        override val locationPlace: LocationPlaceModel?,
        override val originalCreatedAt: Date,
        override val originalId: String,
        override val privacyConnections: List<String>,
        override val privacyType: NewsFeedItemPrivacyType,
        override val tags: List<String>,
        override val text: String?,
        override val updatedAt: Date,
        override val counters: NewsFeedItemCounters,
        override val author: ShortAccountModel,
        override val copyOwnerId: String
) : NewsFeedItemModel

data class NewsFeedItemCountersImpl(
        override val comments: Int,
        override val likes: Int,
        override val recommend: Int,
        override val reposts: Int,
        override val unreadResponse: Int,
        override val views: Int
) : NewsFeedItemCounters