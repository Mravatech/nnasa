package com.mnassa.domain.interactor

import android.net.Uri
import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/16/2018.
 */
interface PostsInteractor {
    suspend fun loadAll(): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>>
    suspend fun loadById(id: String): ReceiveChannel<PostModel?>
    suspend fun loadAllUserPostByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun onItemViewed(item: PostModel)
    suspend fun onItemOpened(item: PostModel)
    suspend fun resetCounter()

    suspend fun createNeed(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<TagModel>,
            price: Long?,
            placeId: String?): PostModel

    suspend fun updateNeed(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            tags: List<TagModel>,
            price: Long?,
            placeId: String?)

    suspend fun createGeneralPost(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<TagModel>,
            placeId: String?): PostModel

    suspend fun updateGeneralPost(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            tags: List<TagModel>,
            placeId: String?)


    suspend fun createUserRecommendation(
            accountId: String,
            text: String,
            privacy: PostPrivacyOptions)

    suspend fun updateUserRecommendation(
            postId: String,
            accountId: String,
            text: String)

    suspend fun removePost(postId: String)

    suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel

    suspend fun hideInfoPost(postId: String)

    suspend fun loadOfferCategories(): List<OfferCategoryModel>
}

data class PostPrivacyOptions(
        val privacyType: PostPrivacyType,
        val privacyConnections: Set<String>
)