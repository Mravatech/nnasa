package com.mnassa.domain.repository

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostPrivacyType
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface PostsRepository {
    suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllByAccountUd(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadAllWithPagination(): ReceiveChannel<PostModel>
    suspend fun loadById(id: String): ReceiveChannel<PostModel>
    suspend fun sendViewed(ids: List<String>)
    suspend fun createNeed(text: String, uploadedImagesUrls: List<String>, privacyType: PostPrivacyType, allConnections: Boolean, privacyConnections: List<String>): PostModel
    suspend fun updateNeed(postId: String, text: String, uploadedImagesUrls: List<String>, privacyType: PostPrivacyType, allConnections: Boolean, privacyConnections: List<String>)
    suspend fun removePost(postId: String)
    suspend fun repostPost(postId: String, text: String?, privacyConnections: List<String>): PostModel
}

