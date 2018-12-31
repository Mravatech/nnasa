package com.mnassa.data.repository

import android.arch.persistence.room.Room
import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.convert
import com.mnassa.data.converter.PostAdditionInfo
import com.mnassa.data.database.MnassaDb
import com.mnassa.data.database.entity.PostForUser
import com.mnassa.data.database.entity.PostRoomEntity
import com.mnassa.data.database.entity.PostUserJoin
import com.mnassa.data.database.entity.UserRoomEntity
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
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import java.util.*
import kotlin.collections.set

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

    private var preloadedPosts = HashMap<String, List<PostModel>>() //accountId - to posts future
    private val roomDb by lazy {
        Room.databaseBuilder(context, MnassaDb::class.java, "MnassaDB")
                .fallbackToDestructiveMigration()
                .build()
    }

    override suspend fun loadInfoPosts(): List<InfoPostModel> {
        val accountId = userRepository.getAccountIdOrException()
        return firestoreLockSuspend {
            getAccountsInfoCollectionRef(accountId)
                .awaitList<PostShortDbEntity>()
                .map {
                    async {
                        it.toFullModel(currentUserId = accountId, savePinned = true)
                            ?.let {
                                val additionalInfo = PostAdditionInfo()
                                return@let converter.convert(
                                    it,
                                    additionalInfo,
                                    InfoPostModel::class.java
                                ).apply {
                                    isPinned = true
                                }
                            }
                    }
                }
                .mapNotNull { it.await() }
                .toList()
        }
    }

    override suspend fun loadInfoPostsWithChangesHandling(): ReceiveChannel<ListItemEvent<InfoPostModel>> {
        val accountId = userRepository.getAccountIdOrException()
        return firestoreLockSuspend {
            getAccountsInfoCollectionRef(accountId)
                    .toValueChannelWithChangesHandling<PostShortDbEntity, InfoPostModel>(
                            exceptionHandler = exceptionHandler,
                            queryBuilder = { collection ->
                                collection.orderBy(
                                    PostShortDbEntity::createdAt.name,
                                    // This is loading in a reverse order,
                                    // that's for new items to appear at top of the list
                                    // and not at the bottom.
                                    Query.Direction.ASCENDING
                                )
                            },
                            mapper = {
                                it.toFullModel(currentUserId = accountId, savePinned = true)?.let {
                                    converter.convert(it, PostAdditionInfo(), InfoPostModel::class.java)
                                }?.also { it.isPinned = true }
                            }
                    )
        }
    }

    override suspend fun loadInfoPost(postId: String): PostModel? {
        val accountId = userRepository.getAccountIdOrException()
        return firestoreLockSuspend {
            getAccountsInfoCollectionRef(accountId)
                    .document(postId)
                    .await<PostShortDbEntity>()
                    .let { it?.toFullModel(currentUserId = accountId) }
                    ?: loadById(postId).receiveOrNull()
        }
    }

    private fun getAccountsInfoCollectionRef(accountId: String) = firestore
        .collection(DatabaseContract.TABLE_ACCOUNTS)
        .document(accountId)
        .collection(DatabaseContract.TABLE_INFO_FEED)

    override suspend fun preloadFeed(): List<PostModel> {
        val accountId = userRepository.getAccountIdOrException()
        val posts = roomDb.getUserPostJoinDao().loadPostsByUserId(accountId).mapNotNull { postModel ->
            postModel.toPostModel()
        }
        preloadedPosts[accountId] = posts
        return posts
    }


    override suspend fun getPreloadedFeed(): List<PostModel> {
        return preloadedPosts.getOrPut(userRepository.getAccountIdOrException()) {  preloadFeed()  }.toList()
    }

    override suspend fun loadFeedWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>> {
        val accountId = userRepository.getAccountIdOrException()

        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(DatabaseContract.TABLE_FEED)
                    .toValueChannelWithChangesHandling<PostShortDbEntity, PostModel>(exceptionHandler,
                        queryBuilder = { collection ->
                            collection.orderBy(
                                PostShortDbEntity::createdAt.name,
                                Query.Direction.DESCENDING
                            )
                        }, mapper = {
                        it.toFullModel(currentUserId = accountId)
                    })
        }
    }

    override suspend fun recheckSavedPosts(): ListItemEvent<List<PostModel>> {
        return withContext(DefaultDispatcher) {
            val accountId = userRepository.getAccountIdOrException()
            val newsFeedFuture = firestoreLock {
                firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                        .document(accountId)
                        .collection(DatabaseContract.TABLE_FEED)
                        .awaitList<PostShortDbEntity>()

            }
            val infoFuture = firestoreLock {
                firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                        .document(accountId)
                        .collection(DatabaseContract.TABLE_INFO_FEED)
                        .awaitList<PostShortDbEntity>()
            }
            val itemsFromFirestore = newsFeedFuture.await().toMutableList()
            itemsFromFirestore.addAll(infoFuture.await())
            val idsFromFirestore = itemsFromFirestore.mapNotNull { it.id }
            val idsFromDb = roomDb.getUserPostJoinDao().loadPostsByUserId(accountId).mapNotNull {
                it.toPostModel()
            }.mapNotNull { it.id }.toMutableList()
            idsFromDb.removeAll(idsFromFirestore)
            val removePosts = idsFromDb.mapNotNull {
                val onePost = roomDb.getUserPostJoinDao().getPostAndRemove(it, accountId)
                onePost?.toPostModel()
            }

            ListItemEvent.Removed(removePosts)
        }
    }

    override suspend fun clearSavedPosts() {
        preloadedPosts.clear()
        withContext(DefaultDispatcher){
            roomDb.getUserPostJoinDao().clearAll()
            roomDb
        }
    }

    //==============================================================================================

    override suspend fun preloadWall(accountId: String): List<PostModel> {
        return firestoreLockSuspend {
            val tableName = if (accountId == userRepository.getAccountIdOrException()) DatabaseContract.TABLE_PRIVATE_WALL else DatabaseContract.TABLE_PUBLIC_WALL

            firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(tableName)
                    .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
                    .limit(DEFAULT_LIMIT.toLong())
                    .awaitList<PostShortDbEntity>()
                    .mapNotNull { it.toFullModel() }
        }
    }

    override suspend fun loadWallWithChangesHandling(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        return firestoreLockSuspend {
            val tableName = if (accountId == userRepository.getAccountIdOrException()) DatabaseContract.TABLE_PRIVATE_WALL else DatabaseContract.TABLE_PUBLIC_WALL

            firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(tableName)
                    .toValueChannelWithChangesHandling<PostShortDbEntity, PostModel>(exceptionHandler, mapper = {
                        it.toFullModel()
                    })
        }
    }

    override suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                    .document(groupId)
                    .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_FEED)
                    .toValueChannelWithChangesHandling<PostShortDbEntity, PostModel>(
                            exceptionHandler = exceptionHandler,
                            mapper = { it.toFullModel(groupId = groupId) }
                    )
        }
    }

    override suspend fun preloadGroupFeed(groupId: String): List<PostModel> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_GROUPS_ALL)
                    .document(groupId)
                    .collection(DatabaseContract.TABLE_GROUPS_ALL_COL_FEED)
                    .orderBy(PostDbEntity.PROPERTY_CREATED_AT, Query.Direction.DESCENDING)
                    .limit(DEFAULT_LIMIT.toLong())
                    .awaitList<PostShortDbEntity>()
                    .mapNotNull { it.toFullModel(groupId = groupId) }
        }
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
                allConnections = postModel.privacy.privacyType is PostPrivacyType.PUBLIC,
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
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_OFFER_CATEGORY)
                    .awaitList<OfferCategoryDbModel>()
                    .map { converter.convert(it, OfferCategoryModel::class.java) }
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

    private suspend fun getFromDb(postId: String): PostModel? {
        return withContext(DefaultDispatcher) {
            roomDb.getUserPostJoinDao().getPostById(postId)?.toPostModel()
        }
    }

    private suspend fun getFromFirestoreChannel(postId: String, groupId: String? = null, currentUserId: String? = null, savePinned: Boolean = false): ReceiveChannel<PostModel?> {
        return firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_ALL_POSTS)
                    .document(postId)
                    .toValueChannel<PostDbEntity>(exceptionHandler)
                    .map {
                        withContext(DefaultDispatcher) {
                            if (it == null) {
                                currentUserId?.let { userId ->
                                    roomDb.getUserPostJoinDao().deleteJoin(userId, postId)
                                }
                                null
                            } else {
                                mapPost(it, groupId)?.also {
                                    if (savePinned) {
                                        (it as InfoPostModel).isPinned = true
                                    }
                                    currentUserId?.let { userId ->
                                        roomDb.getUserPostJoinDao().insert(PostForUser(UserRoomEntity(userId), PostRoomEntity(it)))
                                    }
                                }
                            }
                        }
                    }
        }
    }

    private suspend fun PostShortDbEntity.toFullModel(groupId: String? = null, currentUserId: String? = null, savePinned: Boolean = false): PostModel? {
        var result = getFromDb(id)
        if ((result?.updatedAt?.time ?: 0) < updatedAt) {
            result = getFromFirestoreChannel(id, groupId, currentUserId, savePinned).consume { receive() }
        }
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
        val fromNewsFeed = firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(DatabaseContract.TABLE_FEED)
                    .document(id)
                    .await<PostShortDbEntity>()
        }
        if (fromNewsFeed != null) return fromNewsFeed

        val fromPrivateWall = firestoreLockSuspend {
            firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(DatabaseContract.TABLE_PRIVATE_WALL)
                    .document(id)
                    .await<PostShortDbEntity>()
        }
        return fromPrivateWall
    }
}