package com.mnassa.domain.repository

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface PostsRepository {
    suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllWithPagination(): ReceiveChannel<PostModel>
    suspend fun loadById(id: String): ReceiveChannel<PostModel?>
    suspend fun loadUserPostById(id: String, accountId: String): PostModel?
    suspend fun sendViewed(ids: List<String>)
    suspend fun createNeed(
            text: String,
            uploadedImagesUrls: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<String>,
            price: Long?,
            timeOfExpiration: Long?,
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
    suspend fun getDefaultExpirationDays(): Int
    suspend fun createUserRecommendation(accountId: String, text: String, privacy: PostPrivacyOptions)
    suspend fun updateUserRecommendation(postId: String, accountId: String, text: String)

    suspend fun removePost(postId: String)
    suspend fun repostPost(postId: String, text: String?, privacyConnections: Set<String>): PostModel
}

