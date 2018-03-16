package com.mnassa.domain.repository

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostPrivacyType
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface PostsRepository {
    suspend fun loadAll(): ReceiveChannel<ListItemEvent<Post>>
    suspend fun loadAllWithPagination(): ReceiveChannel<Post>
    suspend fun loadById(id: String): Post?
    suspend fun sendViewed(ids: List<String>)
    suspend fun createNeed(text: String, uploadedImagesUrls: List<String>, privacyType: PostPrivacyType, privacyConnections: List<String>): Post
}

