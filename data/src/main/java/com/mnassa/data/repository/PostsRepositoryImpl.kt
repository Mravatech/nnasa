package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.converter.PostAdditionInfo
import com.mnassa.data.extensions.*
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.firebase.OfferCategoryDbModel
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.firebase.PriceDbEntity
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.network.stringValue
import com.mnassa.data.repository.DatabaseContract.TABLE_INFO_FEED
import com.mnassa.data.repository.DatabaseContract.TABLE_NEWS_FEED
import com.mnassa.data.repository.DatabaseContract.TABLE_POSTS
import com.mnassa.data.repository.DatabaseContract.TABLE_PUBLIC_POSTS
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.map
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Peter on 3/15/2018.
 */
class PostsRepositoryImpl(private val db: DatabaseReference,
                          private val firestore: FirebaseFirestore,
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

    override suspend fun loadAllImmediately(): List<PostModel> {
        return db.child(TABLE_NEWS_FEED)
                .child(userRepository.getAccountIdOrException())
                .awaitList<PostDbEntity>(exceptionHandler)
                .mapNotNull { mapPost(it) }
    }

    override suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>> {
        return db.child(TABLE_INFO_FEED)
                .child(userRepository.getAccountIdOrException())
                .toValueChannelWithChangesHandling<PostDbEntity, InfoPostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = {
                            val post = converter.convert(it, PostAdditionInfo(), InfoPostModel::class.java)
                            post.isPinned = true
                            post
                        }
                )
    }

    override suspend fun loadAllByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        val userId = requireNotNull(accountId)
        val table = if (userId == userRepository.getAccountIdOrException()) TABLE_POSTS else TABLE_PUBLIC_POSTS
        return db.child(table)
                .child(userId)
                .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapPost(it) }
                )
    }

    override suspend fun loadAllUserPostByAccountIdImmediately(accountId: String): List<PostModel> {
        val userId = requireNotNull(accountId)
        val table = if (userId == userRepository.getAccountIdOrException()) TABLE_POSTS else TABLE_PUBLIC_POSTS
        return db.child(table)
                .child(userId)
                .awaitList<PostDbEntity>(exceptionHandler)
                .mapNotNull { mapPost(it) }
    }

    override suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                .document(groupId)
                .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_FEED)
                .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(
                        exceptionHandler = exceptionHandler,
                        mapper = { mapPost(it, groupId) }
                )
    }

    override suspend fun loadAllByGroupIdImmediately(groupId: String): List<PostModel> {
        return firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                .document(groupId)
                .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_FEED)
                .awaitList<PostDbEntity>()
                .mapNotNull { mapPost(it, groupId) }
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

    override suspend fun loadById(id: String, authorId: String): ReceiveChannel<PostModel?> {
        //load from newsFeed -> if not found load from public posts
        val newsFeedRef = db.child(TABLE_NEWS_FEED).child(userRepository.getAccountIdOrException()).child(id)
        val publicPostRef = db.child(TABLE_PUBLIC_POSTS).child(authorId).child(id)

        val isPublicPost = publicPostRef.await<PostDbEntity>(exceptionHandler)?.type != null

        return (if (isPublicPost) publicPostRef else newsFeedRef)
                .toValueChannel<PostDbEntity>(exceptionHandler)
                .map { it?.run { mapPost(this) } }

    }

    override suspend fun loadUserPostById(id: String, accountId: String): PostModel? {
        return loadById(id, accountId).consume { receive() }
    }

    override suspend fun sendViewed(ids: List<String>) = postApi.viewItems(ViewItemsRequest(ids, NetworkContract.EntityType.POST)).handleException(exceptionHandler).run { Unit }
    override suspend fun sendOpened(ids: List<String>) = postApi.openItem(OpenItemsRequest(ids.first(), NetworkContract.EntityType.POST)).handleException(exceptionHandler).run { Unit }
    override suspend fun resetCounter() = postApi.resetCounter(ResetCounterRequest(NetworkContract.ResetCounter.POSTS)).handleException(exceptionHandler).run { Unit }

    override suspend fun createNeed(post: RawPostModel): PostModel = processPost(NetworkContract.PostType.NEED, post).let { converter.convert(it, PostAdditionInfo.withGroup(post.groupIds)) }
    override suspend fun updateNeed(post: RawPostModel) = processPost(NetworkContract.PostType.NEED, post).run { Unit }
    override suspend fun createGeneralPost(post: RawPostModel): PostModel = processPost(NetworkContract.PostType.GENERAL, post).let { converter.convert(it, PostAdditionInfo.withGroup(post.groupIds)) }
    override suspend fun updateGeneralPost(post: RawPostModel) = processPost(NetworkContract.PostType.GENERAL, post).run { Unit }
    override suspend fun createOffer(post: RawPostModel): OfferPostModel = processPost(NetworkContract.PostType.OFFER, post).let { converter.convert(it, PostAdditionInfo.withGroup(post.groupIds)) }
    override suspend fun updateOffer(post: RawPostModel) = processPost(NetworkContract.PostType.OFFER, post).run { Unit }

    override suspend fun createUserRecommendation(post: RawRecommendPostModel) {
        postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.ACCOUNT,
                accountForRecommendation = post.accountId,
                text = post.text,
                privacyType = post.privacy.privacyType.stringValue,
                privacyConnections = post.privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = post.privacy.privacyType is PostPrivacyType.PUBLIC,
                groups = post.groupIds.toList().takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
    }

    override suspend fun updateUserRecommendation(post: RawRecommendPostModel) {
        postApi.changePost(CreatePostRequest(
                type = NetworkContract.PostType.ACCOUNT,
                accountForRecommendation = post.accountId,
                text = post.text,
                postId = post.postId,
                groups = post.groupIds.toList().takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
    }

    private suspend fun processPost(postType: String, postModel: RawPostModel): Any {
        val request = CreatePostRequest(
                type = postType,
                postId = postModel.id,
                groups = postModel.groupIds.takeIf { it.isNotEmpty() },
                text = postModel.text,
                location = postModel.placeId,
                tags = postModel.processedTags.takeIf { it.isNotEmpty() },
                images = postModel.processedImages.takeIf { it.isNotEmpty() },
                privacyType = postModel.privacy.privacyType.stringValue,
                allConnections = postModel.privacy.privacyType is PostPrivacyType.PUBLIC,
                privacyConnections = postModel.privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                price = postModel.price,
                timeOfExpiration = postModel.timeOfExpiration,
                //offer:
                title = postModel.title,
                category = postModel.category?.name?.engTranslate, //TODO: move to category.id (backend, iOS)
                subcategory = postModel.subCategory?.name?.engTranslate
        )

        return if (postModel.id == null) {
            postApi.createPost(request).handleException(exceptionHandler).data
        } else postApi.changePost(request).handleException(exceptionHandler)

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

    override suspend fun getDefaultExpirationDays(): Long {
        return db.child(DatabaseContract.TABLE_CLIENT_DATA)
                .child(DatabaseContract.TABLE_CLIENT_DATA_COL_DEFAULT_EXPIRATION_TIME)
                .await(exceptionHandler)!!
    }

    override suspend fun hideInfoPost(postId: String) {
        postApi.hideInfoPost(HideInfoPostRequest(postId)).handleException(exceptionHandler)
    }

    override suspend fun loadOfferCategories(): List<OfferCategoryModel> {
        return firestore.collection(DatabaseContract.TABLE_OFFER_CATEGORY)
                .awaitList<OfferCategoryDbModel>()
                .map { converter.convert(it, OfferCategoryModel::class.java) }
    }

    private val tagsCache = ConcurrentHashMap<String, TagModel>()
    private suspend fun mapPost(input: PostDbEntity, groupId: String? = null): PostModel? {
        return try {
            val out: PostModel = converter.convert(input, PostAdditionInfo.withGroup(groupId))
            if (out is RecommendedProfilePostModel) {
                val offerIds = input.postedAccount?.values?.firstOrNull()?.offers ?: emptyList()
                //todo: performance issue
//                out.offers = offerIds.map { async { tagsCache.getOrPut(it) { tagRepository.get(it) } } }.mapNotNull { it.await() }
            }
            out
        } catch (e: Exception) {
            Timber.e(e, "Error while mapping post ${input.id}; groupId = $groupId")
            null
        }
    }
}