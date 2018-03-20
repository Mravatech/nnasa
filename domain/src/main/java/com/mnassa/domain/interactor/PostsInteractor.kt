package com.mnassa.domain.interactor

import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostPrivacyType
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import java.io.File

/**
 * Created by Peter on 3/16/2018.
 */
interface PostsInteractor {
    suspend fun loadAll(): ReceiveChannel<ListItemEvent<Post>>
    suspend fun loadById(id: String): ReceiveChannel<Post>
    fun onItemViewed(item: Post)

    suspend fun createNeed(
            text: String,
            images: List<File>,
            privacyType: PostPrivacyType,
            privacyConnections: List<String>): Post

}