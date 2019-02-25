package com.mnassa.data.repository

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.convert
import com.mnassa.data.converter.PostAdditionInfo
import com.mnassa.data.extensions.*
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.firebase.OfferCategoryDbModel
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.data.network.bean.firebase.PostShortDbEntity
import com.mnassa.data.network.bean.firebase.PriceDbEntity
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.network.stringValue
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.withContext
import timber.log.Timber

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
                          private val context: Context) : PostsRepository {

    override suspend fun loadInfoPosts(): List<InfoPostModel> {
        val serialNumber = userRepository.getSerialNumberOrException()
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .whereArrayContains(PostDbEntity.INFO_FOR_USERS, serialNumber)
            .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
            .awaitList<PostDbEntity>()
            .mapNotNull { mapInfoPost(it) }
            .toList()
    }

    override suspend fun loadInfoPostsWithChangesHandling(): ReceiveChannel<ListItemEvent<InfoPostModel>> {
        val serialNumber = userRepository.getSerialNumberOrException()
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .toValueChannelWithChangesHandling<PostDbEntity, InfoPostModel>(
                exceptionHandler = exceptionHandler,
                queryBuilder = { collection ->
                    collection
                        .whereArrayContains(PostDbEntity.INFO_FOR_USERS, serialNumber)
                        .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
                },
                mapper = {
                    mapInfoPost(it)
                }
            )

    }

    override suspend fun loadInfoPost(postId: String): PostModel? {
        return loadById(postId).consume { receiveOrNull() }
    }

    override suspend fun loadFeedWithChangesHandling(pagination: PaginationController?): ReceiveChannel<ListItemEvent<PostModel>> {
        val serialNumber = userRepository.getSerialNumberOrException()
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(exceptionHandler,
                pagination = pagination,
                queryBuilder = { collection ->
                    collection
                        .whereArrayContains(PostDbEntity.VISIBLE_FOR_USERS, serialNumber)
                        .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
                }, mapper = { postDb ->
                    mapPost(postDb, null)
                })
    }

    //==============================================================================================

    override suspend fun preloadWall(accountId: String): List<PostModel> {
        val serialNumber = userRepository.getSerialNumberOrException()
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .whereArrayContains(PostDbEntity.VISIBLE_FOR_USERS, serialNumber)
            .whereEqualTo(PostDbEntity.AUTHOR_ID, accountId)
            .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
            .limit(DEFAULT_LIMIT.toLong())
            .awaitList<PostShortDbEntity>()
            .mapNotNull { it.toFullModel() }
    }

    override suspend fun loadWallWithChangesHandling(accountId: String, pagination: PaginationController?): ReceiveChannel<ListItemEvent<PostModel>> {
        val serialNumber = userRepository.getSerialNumberOrException()
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .toValueChannelWithChangesHandling<PostShortDbEntity, PostModel>(
                exceptionHandler,
                queryBuilder = { collection ->
                    collection
                        .whereArrayContains(PostDbEntity.VISIBLE_FOR_USERS, serialNumber)
                        .whereEqualTo(PostDbEntity.AUTHOR_ID, accountId)
                        .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
                },
                pagination = pagination,
                mapper = {
                    it.toFullModel()
                }
            )
    }

    override suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .toValueChannelWithChangesHandling<PostDbEntity, PostModel>(
                exceptionHandler = exceptionHandler,
                queryBuilder = { collection ->
                    collection
                        .whereArrayContains(PostDbEntity.VISIBLE_FOR_GROUPS, groupId)
                        .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
                },
                mapper = { mapPost(it, groupId) }
            )
    }

    override suspend fun preloadGroupFeed(groupId: String): List<PostModel> {
        return firestore
            .collection(DatabaseContract.TABLE_ALL_POSTS)
            .whereArrayContains(PostDbEntity.VISIBLE_FOR_GROUPS, groupId)
            .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
            .limit(DEFAULT_LIMIT.toLong())
            .awaitList<PostDbEntity>()
            .mapNotNull { mapPost(it, groupId) }
    }

    override suspend fun loadById(id: String): ReceiveChannel<PostModel?> = getFromFirestoreChannel(id)

    override suspend fun sendViewed(ids: List<String>) = postApi.viewItems(ViewItemsRequest(ids, NetworkContract.EntityType.POST)).handleException(exceptionHandler).run { Unit }
    override suspend fun sendOpened(ids: List<String>) = postApi.openItem(OpenItemsRequest(ids.first(), NetworkContract.EntityType.POST)).handleException(exceptionHandler).run { Unit }
    override suspend fun resetCounter() = postApi.resetCounter(ResetCounterRequest(NetworkContract.ResetCounter.POSTS)).handleException(exceptionHandler).run { Unit }

    override suspend fun createNeed(post: RawPostModel) = processPost(NetworkContract.PostType.NEED, post)
    override suspend fun updateNeed(post: RawPostModel) = processPost(NetworkContract.PostType.NEED, post)
    override suspend fun changeStatus(id: String, status: ExpirationType) {
        val statusString = when (status) {
            is ExpirationType.ACTIVE -> DatabaseContract.EXPIRATION_TYPE_ACTIVE
            is ExpirationType.FULFILLED -> DatabaseContract.EXPIRATION_TYPE_FULFILLED
            is ExpirationType.CLOSED -> DatabaseContract.EXPIRATION_TYPE_CLOSED
            is ExpirationType.EXPIRED -> DatabaseContract.EXPIRATION_TYPE_EXPIRED
        }
        postApi.changePostStatus(ChangePostStatusRequest(id, statusString)).handleException(exceptionHandler)
    }

    override suspend fun createGeneralPost(post: RawPostModel) = processPost(NetworkContract.PostType.GENERAL, post)
    override suspend fun updateGeneralPost(post: RawPostModel) = processPost(NetworkContract.PostType.GENERAL, post)
    override suspend fun createOffer(post: RawPostModel) = processPost(NetworkContract.PostType.OFFER, post)
    override suspend fun updateOffer(post: RawPostModel) = processPost(NetworkContract.PostType.OFFER, post)

    override suspend fun createUserRecommendation(post: RawRecommendPostModel) {
        postApi.createPost(CreatePostRequest(
                type = NetworkContract.PostType.ACCOUNT,
                accountForRecommendation = post.accountId,
                text = post.text,
                privacyType = post.privacy.privacyType.stringValue,
                privacyConnections = post.privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                allConnections = post.privacy.privacyType is PostPrivacyType.PUBLIC && post.privacy.privacyCommunitiesIds.isEmpty(),
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

    private suspend fun processPost(postType: String, postModel: RawPostModel) {
        val request = CreatePostRequest(
                type = postType,
                postId = postModel.id,
                groups = postModel.groupIds.takeIf { it.isNotEmpty() },
                text = postModel.text,
                location = postModel.placeId,
                tags = postModel.processedTags.takeIf { it.isNotEmpty() },
                images = postModel.processedImages.takeIf { it.isNotEmpty() },
                privacyType = postModel.privacy.privacyType.stringValue,
                allConnections = postModel.privacy.privacyType is PostPrivacyType.PUBLIC && postModel.privacy.privacyCommunitiesIds.isEmpty(),
                privacyConnections = postModel.privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList(),
                price = postModel.price,
                timeOfExpiration = postModel.timeOfExpiration,
                //offer:
                title = postModel.title,
                category = postModel.category?.name?.engTranslate, //TODO: move to category.id (backend, iOS)
                subcategory = postModel.subCategory?.name?.engTranslate
        )

        if (postModel.id == null) {
            postApi.createPost(request).handleException(exceptionHandler)
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

    private suspend fun mapInfoPost(input: PostDbEntity): InfoPostModel? {
        return mapPost(input, null)
            ?.let { it as InfoPostModel }
            ?.apply {
                isPinned = true
            }
    }

    private suspend fun mapPost(input: PostDbEntity, groupId: String? = null): PostModel? {
        return try {
            return converter.convert(input.withAutoSuggest(), PostAdditionInfo.withGroup(groupId))
        } catch (e: Exception) {
            Timber.e(e, "Error while mapping post ${input.id}; groupId = $groupId")
            null
        }
    }

    private suspend fun getFromFirestoreChannel(postId: String, groupId: String? = null, currentUserId: String? = null, savePinned: Boolean = false): ReceiveChannel<PostModel?> {
        return firestore.collection(DatabaseContract.TABLE_ALL_POSTS)
                    .document(postId)
                    .toValueChannel<PostDbEntity>(exceptionHandler)
                    .map {
                        withContext(Dispatchers.Default) {
                            if (it == null) {
                                null
                            } else {
                                mapPost(it, groupId)?.also {
                                    if (savePinned) {
                                        (it as InfoPostModel).isPinned = true
                                    }
                                }
                            }
                    }
        }
    }

    private suspend fun PostShortDbEntity.toFullModel(groupId: String? = null, currentUserId: String? = null, savePinned: Boolean = false): PostModel? {
        var result = getFromFirestoreChannel(id, groupId, currentUserId, savePinned).consume { receive() }
        result?.autoSuggest = this.autoSuggest ?: PostAutoSuggest.EMPTY

        return result
    }

    private suspend fun PostDbEntity.withAutoSuggest(): PostDbEntity {
        if (autoSuggest != null) return this
        autoSuggest = loadShortModel(id)?.autoSuggest
        return this
    }

    private suspend fun loadShortModel(id: String): PostShortDbEntity? {
        val accountId = userRepository.getAccountIdOrException()
        val fromNewsFeed = firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(DatabaseContract.TABLE_FEED)
                    .document(id)
                    .await<PostShortDbEntity>()
        if (fromNewsFeed != null) return fromNewsFeed

        val fromPrivateWall = firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(DatabaseContract.TABLE_PRIVATE_WALL)
                    .document(id)
                    .await<PostShortDbEntity>()
        return fromPrivateWall
    }
}