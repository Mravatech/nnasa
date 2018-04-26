package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.extensions.toValueChannelWithChangesHandling
import com.mnassa.data.extensions.toValueChannelWithPagination
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.retrofit.request.CreatePostRequest
import com.mnassa.data.network.bean.retrofit.request.RepostCommentRequest
import com.mnassa.data.network.bean.retrofit.request.ViewItemsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.network.stringValue
import com.mnassa.data.repository.DatabaseContract.TABLE_NEWS_FEED
import com.mnassa.data.repository.DatabaseContract.TABLE_PABLIC_POSTS
import com.mnassa.data.repository.DatabaseContract.TABLE_POSTS
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.RecommendedProfilePostModel
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/15/2018.
 */
class PostsRepositoryImpl(private val db: DatabaseReference,
                          private val userRepository: UserRepository,
                          private val tagRepository: TagRepository,
                          private val exceptionHandler: ExceptionHandler,
                          private val converter: ConvertersContext,
                          private val postApi: FirebasePostApi) : PostsRepository {

    override suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>> {
        return db.child(TABLE_NEWS_FEED)
                .child(userRepository.getAccountIdOrException())
                .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapPost(it) }
                )
    }

    override suspend fun loadAllByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        val userId = requireNotNull(accountId)
        val table = if (userId == userRepository.getAccountIdOrException()) TABLE_POSTS else TABLE_PABLIC_POSTS
        return db.child(table)
                .child(userId)
                .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapPost(it) }
                )
    }

    override suspend fun loadAllWithPagination(): ReceiveChannel<PostModel> {
        val userId = requireNotNull(userRepository.getAccountIdOrException())

        return db
                .child(TABLE_NEWS_FEED)
                .child(userId)
                .toValueChannelWithPagination<PostDbEntity, PostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapPost(it) })
    }

    override suspend fun loadById(id: String): ReceiveChannel<PostModel?> {
        val userId = requireNotNull(userRepository.getAccountIdOrException())

        return db
                .child(TABLE_NEWS_FEED)
                .child(userId)
                .child(id)
                .toValueChannel<PostDbEntity>(exceptionHandler)
                .map { it?.run { mapPost(this) } }
    }

    override suspend fun loadUserPostById(id: String, accountId: String): PostModel? {
        return db
                .child(TABLE_NEWS_FEED)
                .child(accountId)
                .child(id)
                .await<PostDbEntity>(exceptionHandler)
                ?.run { converter.convert(this, PostModel::class.java) }
    }

    override suspend fun sendViewed(ids: List<String>) {
        postApi.viewItems(ViewItemsRequest(ids, NetworkContract.EntityType.POST)).handleException(exceptionHandler)
    }

    override suspend fun createNeed(
            text: String,
            uploadedImagesUrls: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<String>,
            price: Long?,
            placeId: String?
    ): PostModel {
        val result = postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.NEED,
                text = text,
                images = uploadedImagesUrls.takeIf { it.isNotEmpty() },
                privacyType = privacy.privacyType.stringValue,
                privacyConnections = privacy.privacyConnections.takeIf { it.isNotEmpty() },
                allConnections = privacy.privacyType == PostPrivacyType.PUBLIC,
                tags = tags,
                price = price,
                location = placeId
        )).handleException(exceptionHandler)
        return result.data.run { converter.convert(this) }
    }

    override suspend fun updateNeed(
            postId: String,
            text: String,
            uploadedImagesUrls: List<String>,
            tags: List<String>,
            price: Long?,
            placeId: String?
    ) {
        postApi.changePost(CreatePostRequest(
                postId = postId,
                type = NetworkContract.PostType.NEED,
                text = text,
                images = uploadedImagesUrls.takeIf { it.isNotEmpty() },
                tags = tags,
                price = price,
                location = placeId
        )).handleException(exceptionHandler)
    }

    override suspend fun createUserRecommendation(accountId: String, text: String, privacy: PostPrivacyOptions) {
        postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.ACCOUNT,
                accountForRecommendation = accountId,
                text = text,
                privacyType = privacy.privacyType.stringValue,
                privacyConnections = privacy.privacyConnections.takeIf { it.isNotEmpty() },
                allConnections = privacy.privacyType == PostPrivacyType.PUBLIC
        )).handleException(exceptionHandler)
    }

    override suspend fun updateUserRecommendation(postId: String, accountId: String, text: String) {
        postApi.changePost(CreatePostRequest(
                type = NetworkContract.PostType.ACCOUNT,
                accountForRecommendation = accountId,
                text = text,
                postId = postId
        )).handleException(exceptionHandler)
    }

    override suspend fun removePost(postId: String) {
        postApi.deletePost(postId).handleException(exceptionHandler)
    }

    override suspend fun repostPost(postId: String, text: String?, privacyConnections: List<String>): PostModel {
        return postApi.repostComment(RepostCommentRequest(postId, text?.takeIf { it.isNotBlank() }, privacyConnections.takeIf { it.isNotEmpty() }))
                .handleException(exceptionHandler)
                .data
                .run { converter.convert(this) }
    }

    private suspend fun mapPost(input: PostDbEntity): PostModel {
        val out: PostModel = converter.convert(input)
        if (out is RecommendedProfilePostModel) {
            //TODO: create converter, which supports suspend functions
            val offerIds = input.postedAccount?.values?.firstOrNull()?.offers ?: emptyList()
            out.offers = offerIds.mapNotNull { tagRepository.get(it) }
        }
        return out
    }
}