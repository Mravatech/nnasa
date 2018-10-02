package com.mnassa.domain.repository

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface PostsRepository {
    //personal feed
    suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>>
    suspend fun loadInfoPost(postId: String): PostModel?

    //
    suspend fun preloadFeed(): List<PostModel>
    suspend fun getPreloadedFeed(): List<PostModel>
    suspend fun loadFeedWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>>

    //account wall
    suspend fun preloadWall(accountId: String): List<PostModel>
    suspend fun loadWallWithChangesHandling(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>

    //group wall
    suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun preloadGroupFeed(groupId: String): List<PostModel>

    suspend fun loadById(id: String): ReceiveChannel<PostModel?>

    //counters logic
    suspend fun sendViewed(ids: List<String>)
    suspend fun sendOpened(ids: List<String>)
    suspend fun resetCounter()

    //
    suspend fun createNeed(post: RawPostModel)
    suspend fun updateNeed(post: RawPostModel)

    //post expiration logic
    suspend fun getDefaultExpirationDays(): Long
    suspend fun changeStatus(id: String, status: ExpirationType)

    suspend fun createGeneralPost(post: RawPostModel)
    suspend fun updateGeneralPost(post: RawPostModel)
    suspend fun createOffer(post: RawPostModel)
    suspend fun updateOffer(post: RawPostModel)
    suspend fun createUserRecommendation(post: RawRecommendPostModel)
    suspend fun updateUserRecommendation(post: RawRecommendPostModel)
    //

    suspend fun getShareOfferPostPrice(): Long?
    suspend fun getShareOfferPostPerUserPrice(): Long?
    suspend fun getPromotePostPrice(): Long?
    suspend fun promote(post: PostModel)

    suspend fun removePost(postId: String)
    suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel
    suspend fun hideInfoPost(postId: String)

    suspend fun loadOfferCategories(): List<OfferCategoryModel>
}

