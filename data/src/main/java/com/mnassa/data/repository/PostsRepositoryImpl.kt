package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.extensions.toValueChannelWithPagination
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.retrofit.request.CreatePostRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.data.network.stringValue
import com.mnassa.data.repository.DatabaseContract.TABLE_NEWS_FEED
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/15/2018.
 */
class PostsRepositoryImpl(private val db: DatabaseReference,
                          private val userRepository: UserRepository,
                          private val exceptionHandler: ExceptionHandler,
                          private val converter: ConvertersContext,
                          private val postApi: FirebasePostApi) : PostsRepository {

    override suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<Post>> {
        val userId = requireNotNull(userRepository.getAccountId())

        return db.child(TABLE_NEWS_FEED)
                .child(userId)
                .toValueChannelWithChangesHandling<PostDbEntity, Post>(
                        exceptionHandler = exceptionHandler,
                        mapper = converter.convertFunc(Post::class.java)
                )
    }

    override suspend fun loadAllWithPagination(): ReceiveChannel<Post> {
        val userId = requireNotNull(userRepository.getAccountId())

        return db
                .child(TABLE_NEWS_FEED)
                .child(userId)
                .toValueChannelWithPagination<PostDbEntity, Post>(
                        exceptionHandler = exceptionHandler,
                        mapper = converter.convertFunc(Post::class.java))
    }

    override suspend fun loadById(id: String): ReceiveChannel<Post> {
        val userId = requireNotNull(userRepository.getAccountId())

        return db
                .child(TABLE_NEWS_FEED)
                .child(userId)
                .child(id)
                .toValueChannel<PostDbEntity>(exceptionHandler)
                .map { converter.convert<Post>(it!!) }
    }

    override suspend fun sendViewed(ids: List<String>) {
//        postApi.viewItems(ViewItemsRequest(ids, NetworkContract.ItemType.POST)).handleException(exceptionHandler)
    }

    override suspend fun createNeed(text: String, uploadedImagesUrls: List<String>, privacyType: PostPrivacyType, privacyConnections: List<String>): Post {
        val result = postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.NEED,
                text = text,
                images = if (uploadedImagesUrls.isNotEmpty()) uploadedImagesUrls else null,
                privacyType = privacyType.stringValue,
                privacyConnections = if (privacyConnections.isNotEmpty()) privacyConnections else null
        )).handleException(exceptionHandler)
        return result.data.run { converter.convert(this) }
    }

    override suspend fun removePost(postId: String) {
        postApi.deletePost(postId).handleException(exceptionHandler)
    }
}