package com.mnassa.domain.interactor

import android.net.Uri
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
    suspend fun onItemViewed(item: Post)

    suspend fun createNeed(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacyType: PostPrivacyType,
            toAll: Boolean,
            privacyConnections: List<String>): Post

    suspend fun updateNeed(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacyType: PostPrivacyType,
            toAll: Boolean,
            privacyConnections: List<String>)

    suspend fun removePost(postId: String)

}