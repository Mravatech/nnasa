package com.mnassa.data.repository

import android.arch.persistence.room.Room
import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mnassa.data.converter.PostAdditionInfo
import com.mnassa.data.database.MnassaDb
import com.mnassa.data.database.entity.PostRoomEntity
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
import com.mnassa.data.repository.DatabaseContract.TABLE_INFO_FEED
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
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

    private var preloadedPosts = HashMap<String, Deferred<List<PostModel>>>() //accountId - to posts future
    private val roomDb by lazy {
        Room.databaseBuilder(context, MnassaDb::class.java, "MnassaDB")
                .fallbackToDestructiveMigration()
                .build()
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

    override suspend fun preloadFeed(): List<PostModel> {
        Timber.e("preloadFeed >>> ")
        val future = async {
            val accountId = userRepository.getAccountIdOrException()
            firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                    .document(accountId)
                    .collection(DatabaseContract.TABLE_FEED)
                    .awaitList<PostShortDbEntity>()
                    .mapNotNull { it.toFullModel() }
                    .also {
                        Timber.e("preloadFeed >>> loaded all posts! ${it.size}")
                    }
        }
        preloadedPosts[userRepository.getAccountIdOrException()] = future
        return future.await()
    }


    override suspend fun getPreloadedFeed(): List<PostModel> {
        Timber.e("preloadFeed >>> getPreloadedFeed")
        return preloadedPosts.getOrPut(userRepository.getAccountIdOrException()) { async { preloadFeed() } }.await().also {
            Timber.e("preloadFeed >>> getPreloadedFeed >> finished")
        }
    }

    override suspend fun loadFeedWithChangesHandling(): ReceiveChannel<ListItemEvent<PostModel>> {
        val accountId = userRepository.getAccountIdOrException()

        return firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                .document(accountId)
                .collection(DatabaseContract.TABLE_FEED)
                .toValueChannelWithChangesHandling<PostShortDbEntity, PostModel>(exceptionHandler, mapper = {
                    it.toFullModel()
                })
    }

    //==============================================================================================

    override suspend fun loadWall(accountId: String): List<PostModel> {
        return firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                .document(accountId)
                .collection(DatabaseContract.TABLE_WALL)
                .awaitList<PostShortDbEntity>()
                .mapNotNull { it.toFullModel() }
                .also {
                    Timber.e("loadWall >>> loaded all posts! ${it.size}")
                }
    }

    override suspend fun loadWallWithChangesHandling(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> {
        return firestore.collection(DatabaseContract.TABLE_ACCOUNTS)
                .document(accountId)
                .collection(DatabaseContract.TABLE_WALL)
                .toValueChannelWithChangesHandling<PostShortDbEntity, PostModel>(exceptionHandler, mapper = {
                    it.toFullModel()
                })
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
        return firestore.collection(DatabaseContract.TABLE_OFFER_CATEGORY)
                .awaitList<OfferCategoryDbModel>()
                .map { converter.convert(it, OfferCategoryModel::class.java) }
    }

    private suspend fun mapPost(input: PostDbEntity, groupId: String? = null): PostModel? {
        return try {
            val out: PostModel = converter.convert(input, PostAdditionInfo.withGroup(groupId))
            if (out is RecommendedProfilePostModel) {
                val offerIds = input.postedAccount?.offers ?: emptyList()
                out.offers = offerIds.map { async { tagRepository.get(it) } }.mapNotNull { it.await() }
            }
            out
        } catch (e: Exception) {
            Timber.e(e, "Error while mapping post ${input.id}; groupId = $groupId")
            null
        }
    }

    private suspend fun getFromDb(postId: String): PostModel? {
        return withContext(DefaultDispatcher) {
            roomDb.getPostDao().getById(postId)?.toPostModel()
        }
    }

    private suspend fun getFromFirestoreChannel(postId: String, groupId: String? = null): ReceiveChannel<PostModel?> {
        return firestore.collection(DatabaseContract.TABLE_ALL_POSTS)
                .document(postId)
                .toValueChannel<PostDbEntity>(exceptionHandler)
                .map {
                    withContext(DefaultDispatcher) {
                        if (it == null) {
                            roomDb.getPostDao().deleteById(postId)
                            null
                        } else {
                            mapPost(it, groupId)?.also {
                                roomDb.getPostDao().insert(PostRoomEntity(it))
                            }
                        }
                    }
                }
    }

    private suspend fun PostShortDbEntity.toFullModel(): PostModel? {
        var result = getFromDb(id)
        if ((result?.updatedAt?.time ?: 0) < updatedAt) {
            result = getFromFirestoreChannel(id).consume { receive() }
        }
        result?.autoSuggest = this.autoSuggest ?: PostAutoSuggest.EMPTY

        return result
    }
}