package com.mnassa.domain.interactor

import android.net.Uri
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostPrivacyType
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/16/2018.
 */
interface PostsInteractor {
    suspend fun loadAll(): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun loadById(id: String): ReceiveChannel<PostModel>
    suspend fun loadAllUserPostByAccountUd(accountId: String): ReceiveChannel<ListItemEvent<PostModel>>
    suspend fun onItemViewed(item: PostModel)

    suspend fun createNeed(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacyType: PostPrivacyType,
            toAll: Boolean,
            privacyConnections: List<String>): PostModel

    suspend fun updateNeed(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacyType: PostPrivacyType,
            toAll: Boolean,
            privacyConnections: List<String>)

    suspend fun removePost(postId: String)

    suspend fun repostPost(postId: String, text: String?, privacyConnections: List<String>): PostModel
}