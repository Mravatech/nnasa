package com.mnassa.domain.repository

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.InfoPostModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface PostsRepository {
    suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>>
    suspend fun loadAllByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllWithPagination(): ReceiveChannel<PostModel>
    suspend fun loadById(id: String): ReceiveChannel<PostModel?>
    suspend fun loadUserPostById(id: String, accountId: String): PostModel?
    suspend fun sendViewed(ids: List<String>)
    suspend fun sendOpened(ids: List<String>)
    suspend fun resetCounter()
    suspend fun createNeed(
            text: String,
            uploadedImagesUrls: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<String>,
            price: Long?,
            placeId: String?
    ): PostModel

    suspend fun updateNeed(
            postId: String,
            text: String,
            uploadedImagesUrls: List<String>,
            tags: List<String>,
            price: Long?,
            placeId: String?
    )

    suspend fun createGeneralPost(
            text: String,
            uploadedImagesUrls: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<String>,
            placeId: String?
    ): PostModel

    suspend fun updateGeneralPost(
            postId: String,
            text: String,
            uploadedImagesUrls: List<String>,
            tags: List<String>,
            placeId: String?
    )

    suspend fun createUserRecommendation(accountId: String, text: String, privacy: PostPrivacyOptions)
    suspend fun updateUserRecommendation(postId: String, accountId: String, text: String)

    suspend fun removePost(postId: String)
    suspend fun repostPost(postId: String, text: String?, privacyConnections: Set<String>): PostModel
    suspend fun hideInfoPost(postId: String)
}

