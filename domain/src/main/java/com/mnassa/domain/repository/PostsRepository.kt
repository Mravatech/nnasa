package com.mnassa.domain.repository

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface PostsRepository {
    suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllImmediately(): List<PostModel>
    suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>>
    suspend fun loadAllByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllUserPostByAccountIdImmediately(accountId: String): List<PostModel>
    suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllByGroupIdImmediately(groupId: String): List<PostModel>
    suspend fun loadAllWithPagination(): ReceiveChannel<PostModel>
    suspend fun loadById(id: String, authorId: String): ReceiveChannel<PostModel?>
    suspend fun loadUserPostById(id: String, accountId: String): PostModel?
    suspend fun sendViewed(ids: List<String>)
    suspend fun sendOpened(ids: List<String>)
    suspend fun resetCounter()

    suspend fun getDefaultExpirationDays(): Long

    //
    suspend fun createNeed(post: RawPostModel): PostModel
    suspend fun updateNeed(post: RawPostModel)
    suspend fun changeStatus(id: String, status: ExpirationType)

    suspend fun createGeneralPost(post: RawPostModel): PostModel
    suspend fun updateGeneralPost(post: RawPostModel)
    suspend fun createOffer(post: RawPostModel): OfferPostModel
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

