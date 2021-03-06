package com.mnassa.data.network.bean.firebase

import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mnassa.data.network.bean.firebase.adapters.PostAutoSuggestJsonAdapter
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 3/15/2018.
 */
internal data class PostDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("allConnections") var allConnections: Boolean?,
        @SerializedName("copyOwner") var copyOwner: String?,
        @SerializedName("counters") var counters: PostCountersDbEntity?,
        @SerializedName(PROPERTY_CREATED_AT) var createdAt: Long?,
        @SerializedName("images") var images: List<String>?,
        @SerializedName("videos") var videos: List<String>?,
        @SerializedName("itemType") var itemType: String?,
        @SerializedName("type") var type: String?,
        @SerializedName("originalCreatedAt") var originalCreatedAt: Long?,
        @SerializedName("originalId", alternate = ["originalPostId"]) var originalId: String?,
        @SerializedName("privacyConnections") var privacyConnections: List<String>?,
        @SerializedName(PRIVACY_TYPE) var privacyType: String?,
        @SerializedName("text") var text: String?,
        @SerializedName("updatedAt") var updatedAt: Long?,
        @SerializedName("location") var location: LocationDbEntity?,
        @SerializedName("tags") var tags: List<String>?,
        @SerializedName("original") var original: String?,
        @SerializedName("statusOfExpiration") var statusOfExpiration: String?,
        @SerializedName("timeOfExpiration") var timeOfExpiration: Long?,
        @SerializedName("author") var author: JsonObject?,
        @SerializedName("price") var price: Double?,
        @SerializedName("autoSuggest") var autoSuggest: PostAutoSuggest?,
        @SerializedName("repostAuthor") var repostAuthor: JsonObject?,
        //posted account
        @SerializedName("postedAccount") var postedAccount: JsonObject?,
        //info post
        @SerializedName("title") var title: String?, //offer post
        //offer post
        @SerializedName("category") val category: String?,
        @SerializedName("subcategory") val subcategory: String?,

        //for groups
        @SerializedName("privacyCommunitiesIds") val groupIds: Set<String>?,
        @SerializedName("privacyCommunitiesInfo") val groups: List<GroupDbEntity>?

) : HasIdMaybe {
    companion object {
        const val PROPERTY_CREATED_AT = "createdAt"
        const val PRIVACY_TYPE = "privacyType"
        const val AUTHOR_ID = "authorId"
        const val INFO_FOR_USERS = "infoFor"
        const val VISIBLE_FOR_USERS = "visibleFor"
        const val VISIBLE_FOR_GROUPS = "visibleForCommunities"
    }
}

internal data class PostCountersDbEntity(
        @SerializedName("comments") var comments: Int?,
        @SerializedName("likes") var likes: Int?,
        @SerializedName("recommend") var recommend: Int?,
        @SerializedName("reposts") var reposts: Int?,
        @SerializedName("offers") var offers: Int?,
        @SerializedName("unreadResponse") var unreadResponse: Int?,
        @SerializedName("views") var views: Int?
)

@JsonAdapter(PostAutoSuggestJsonAdapter::class)
internal data class PostAutoSuggest(
        override var total: Int,
        override var youCanHelp: Boolean,
        override val accountIds: List<String>
) : com.mnassa.domain.model.PostAutoSuggest

internal class PostShortDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("autoSuggest") var autoSuggest: PostAutoSuggest?,
        @SerializedName("updatedAt") var updatedAt: Long?,
        @SerializedName(PostDbEntity.PROPERTY_CREATED_AT) var createdAt: Long?
): HasIdMaybe

