package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.extensions.*
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.firebase.OfferCategoryDbModel
import com.mnassa.data.network.bean.firebase.PriceDbEntity
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.network.stringValue
import com.mnassa.data.repository.DatabaseContract.TABLE_INFO_FEED
import com.mnassa.data.repository.DatabaseContract.TABLE_NEWS_FEED
import com.mnassa.data.repository.DatabaseContract.TABLE_PABLIC_POSTS
import com.mnassa.data.repository.DatabaseContract.TABLE_POSTS
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 3/15/2018.
 */
class PostsRepositoryImpl(private val db: DatabaseReference,
                          private val firestore: FirebaseFirestore,
                          private val userRepository: UserRepository,
                          private val tagRepository: TagRepository,
                          private val exceptionHandler: ExceptionHandler,
                          private val converter: ConvertersContext,
                          private val postApi: FirebasePostApi,
                          private val languageProvider: LanguageProvider) : PostsRepository {

    override suspend fun loadAllWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>> {
        return db.child(TABLE_NEWS_FEED)
                .child(userRepository.getAccountIdOrException())
                .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapPost(it) }
                )
    }

    override suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>> {
        return db.child(TABLE_INFO_FEED)
                .child(userRepository.getAccountIdOrException())
                .toValueChannelWithChangesHandling<PostDbEntity, InfoPostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = {
                            val post = converter.convert(it, Unit, InfoPostModel::class.java)
                            post.isPinned = true
                            post
                        }
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

    override suspend fun sendOpened(ids: List<String>) {
        postApi.openItem(OpenItemsRequest(ids.first(), NetworkContract.EntityType.POST)).handleException(exceptionHandler)
    }

    override suspend fun resetCounter() {
        postApi.resetCounter(ResetCounterRequest(NetworkContract.ResetCounter.POSTS)).handleException(exceptionHandler)
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
                privacyConnections = privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = privacy.privacyType is PostPrivacyType.PUBLIC,
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

    override suspend fun createGeneralPost(
            text: String,
            uploadedImagesUrls: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<String>,
            placeId: String?
    ): PostModel {
        val result = postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.GENERAL,
                text = text,
                images = uploadedImagesUrls.takeIf { it.isNotEmpty() },
                privacyType = privacy.privacyType.stringValue,
                privacyConnections = privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = privacy.privacyType is PostPrivacyType.PUBLIC,
                tags = tags,
                location = placeId
        )).handleException(exceptionHandler)
        return result.data.run { converter.convert(this) }
    }

    override suspend fun updateGeneralPost(
            postId: String,
            text: String,
            uploadedImagesUrls: List<String>,
            tags: List<String>,
            placeId: String?
    ) {
        postApi.changePost(CreatePostRequest(
                postId = postId,
                type = NetworkContract.PostType.GENERAL,
                text = text,
                images = uploadedImagesUrls.takeIf { it.isNotEmpty() },
                tags = tags,
                location = placeId
        )).handleException(exceptionHandler)
    }

    override suspend fun createOffer(
            title: String,
            offer: String,
            category: OfferCategoryModel?,
            subCategory: OfferCategoryModel?,
            tags: List<String>,
            uploadedImagesUrls: List<String>,
            placeId: String?,
            price: Long?,
            postPrivacyOptions: PostPrivacyOptions
    ): OfferPostModel {
        val result = postApi.createPost(CreatePostRequest(
                title = title,
                text = offer,
                type = NetworkContract.PostType.OFFER,
                category = category?.name?.engTranslate, //TODO: move to category.id (backend, iOS)
                subcategory = subCategory?.name?.engTranslate,
                tags = tags,
                images = uploadedImagesUrls.takeIf { it.isNotEmpty() },
                location = placeId,
                price = price,
                privacyType = postPrivacyOptions.privacyType.stringValue,
                privacyConnections = postPrivacyOptions.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = postPrivacyOptions.privacyType is PostPrivacyType.PUBLIC
        )).handleException(exceptionHandler)
        return result.data.run { converter.convert(this) }
    }

    override suspend fun updateOffer(
            postId: String,
            title: String,
            offer: String,
            category: OfferCategoryModel?,
            subCategory: OfferCategoryModel?,
            tags: List<String>,
            uploadedImagesUrls: List<String>,
            placeId: String?,
            price: Long?,
            postPrivacyOptions: PostPrivacyOptions
    ) {
        postApi.changePost(CreatePostRequest(
                postId = postId,
                title = title,
                text = offer,
                type = NetworkContract.PostType.OFFER,
                category = category?.name?.engTranslate, //TODO: move to category.id (backend, iOS)
                subcategory = subCategory?.name?.engTranslate,
                tags = tags,
                images = uploadedImagesUrls.takeIf { it.isNotEmpty() },
                location = placeId,
                price = price,
                privacyType = postPrivacyOptions.privacyType.stringValue,
                privacyConnections = postPrivacyOptions.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = postPrivacyOptions.privacyType is PostPrivacyType.PUBLIC
        )).handleException(exceptionHandler)
    }

    override suspend fun getShareOfferPostPrice(): Long? {
        return db.child(DatabaseContract.SHARE_OFFER_POST)
                .await<PriceDbEntity>(exceptionHandler)
                ?.takeIf { it.state }
                ?.amount
    }

    override suspend fun getShareOfferPostPerUserPrice(): Long? {
        return db.child(DatabaseContract.SHARE_OFFER_POST_PER_USER)
                .await<PriceDbEntity>(exceptionHandler)
                ?.takeIf { it.state }
                ?.amount
    }

    override suspend fun getPromotePostPrice(): Long? {
        return db.child(DatabaseContract.PROMOTE_POST)
                .await<PriceDbEntity>(exceptionHandler)
                ?.takeIf { it.state }
                ?.amount
    }

    override suspend fun promote(post: PostModel) {
        postApi.promote(PromotePostRequest(post.id, converter.convert(post.type))).handleException(exceptionHandler)
    }

    override suspend fun createUserRecommendation(accountId: String, text: String, privacy: PostPrivacyOptions) {
        postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.ACCOUNT,
                accountForRecommendation = accountId,
                text = text,
                privacyType = privacy.privacyType.stringValue,
                privacyConnections = privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = privacy.privacyType is PostPrivacyType.PUBLIC
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

    override suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel {
        return postApi.repostComment(RepostCommentRequest(
                postId = postId,
                text = text?.takeIf { it.isNotBlank() },
                privacyConnections = privacy.privacyConnections.toList(),
                allConnections = privacy.privacyType is PostPrivacyType.PUBLIC))
                .handleException(exceptionHandler)
                .data
                .run { converter.convert(this) }
    }

    override suspend fun hideInfoPost(postId: String) {
        postApi.hideInfoPost(HideInfoPostRequest(postId)).handleException(exceptionHandler)
    }

    override suspend fun loadOfferCategories(): List<OfferCategoryModel> {
        return firestore.collection(DatabaseContract.TABLE_OFFER_CATEGORY)
                .awaitList<OfferCategoryDbModel>()
                .map { converter.convert(it, OfferCategoryModel::class.java) }
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