package com.mnassa.data.network.bean.firebase

import android.support.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 3/15/2018.
 */
internal data class PostDbEntity(
        @SerializedName("id")
        @Nullable
        override var id: String,
        @SerializedName("allConnections") var allConnections: Boolean,
        @SerializedName("copyOwner") var copyOwner: String?,
        @SerializedName("counters") var counters: PostCountersDbEntity,
        @SerializedName("createdAt") var createdAt: Long,
        @SerializedName("images") var images: List<String>?,
        @SerializedName("itemType") var itemType: String,
        @SerializedName("type") var type: String,
        @SerializedName("originalCreatedAt") var originalCreatedAt: Long,
        @SerializedName("originalId") var originalId: String,
        @SerializedName("privacyConnections") var privacyConnections: List<String>?,
        @SerializedName("privacyType") var privacyType: String,
        @SerializedName("text") var text: String?,
        @SerializedName("updatedAt") var updatedAt: Long,
//        @SerializedName("location") var location: LocationDbEntity?,
        @SerializedName("tags") var tags: List<String>?,
        @SerializedName("original") var original: String?,
        @SerializedName("author") var author: Map<String, ShortAccountDbEntity>,
        @SerializedName("price") var price: Double?
) : HasId

internal data class PostCountersDbEntity(
        @SerializedName("comments") var comments: Int,
        @SerializedName("likes") var likes: Int,
        @SerializedName("recommend") var recommend: Int,
        @SerializedName("reposts") var reposts: Int,
        @SerializedName("unreadResponse") var unreadResponse: Int,
        @SerializedName("views") var views: Int
)

