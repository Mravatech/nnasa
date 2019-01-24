package com.mnassa.domain.interactor

import com.mnassa.domain.model.*
import com.mnassa.domain.pagination.PaginationController
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import java.io.Serializable

/**
 * Created by Peter on 3/16/2018.
 */
interface PostsInteractor {
    val mergedInfoPostsAndFeedPagination: PaginationController

    suspend fun cleanMergedInfoPostsAndFeed()
    suspend fun loadMergedInfoPostsAndFeed(): ReceiveChannel<ListItemEvent<List<PostModel>>>

    //personal feed
    suspend fun loadInfoPosts(): List<InfoPostModel>
    suspend fun loadInfoPostsWithChangesHandling(): ReceiveChannel<ListItemEvent<InfoPostModel>>
    suspend fun loadInfoPost(postId: String): PostModel?
    //
    suspend fun recheckAndReloadFeeds(): ListItemEvent<List<PostModel>>

    //account wall
    suspend fun loadWallWithChangesHandling(accountId: String): ReceiveChannel<ListItemEvent<List<PostModel>>>

    //group wall
    suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllByGroupIdImmediately(groupId: String): List<PostModel>

    suspend fun loadById(id: String): ReceiveChannel<PostModel?>

    //
    suspend fun onItemViewed(item: PostModel)
    suspend fun onItemOpened(item: PostModel)
    suspend fun resetCounter()

    suspend fun createNeed(post: RawPostModel)
    suspend fun updateNeed(post: RawPostModel)
    suspend fun changeStatus(id: String, status: ExpirationType)

    suspend fun createGeneralPost(post: RawPostModel)
    suspend fun updateGeneralPost(post: RawPostModel)
    suspend fun createOffer(post: RawPostModel)
    suspend fun updateOffer(post: RawPostModel)
    suspend fun createUserRecommendation(post: RawRecommendPostModel)
    suspend fun updateUserRecommendation(post: RawRecommendPostModel)
    suspend fun removePost(postId: String)

    suspend fun getShareOfferPostPrice(): Long?
    suspend fun getShareOfferPostPerUserPrice(): Long?
    suspend fun getPromotePostPrice(): Long

    suspend fun getDefaultExpirationDays(): Long
    suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel

    suspend fun hideInfoPost(postId: String)
    suspend fun loadOfferCategories(): List<OfferCategoryModel>
    suspend fun promote(post: PostModel)
}

data class PostPrivacyOptions(
        var privacyType: PostPrivacyType,
        var privacyConnections: Set<String>,
        var privacyCommunitiesIds: Set<String>
) : Serializable {
    companion object {
        val DEFAULT = PostPrivacyOptions(PostPrivacyType.PUBLIC(), emptySet(), emptySet())
    }
}