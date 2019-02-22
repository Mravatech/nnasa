package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import kotlin.coroutines.coroutineContext

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository,
                          private val userRepository: UserRepository,
                          private val storageInteractor: StorageInteractor,
                          private val tagInteractor: TagInteractor,
                          private val userProfileInteractorImpl: UserProfileInteractor) : PostsInteractor {

    override val mergedInfoPostsAndFeedPagination = PaginationController(MERGED_FEED_INITIAL_SIZE)

    /**
     * Cached open channels of merged info-posts
     * and feed.
     */
    private var mergedInfoPostsAndFeedMap = HashMap<String, ReceiveChannel<ListItemEvent<List<PostModel>>>>()

    override suspend fun cleanMergedInfoPostsAndFeed() {
        mergedInfoPostsAndFeedMap.clear()
        mergedInfoPostsAndFeedPagination.size = MERGED_FEED_INITIAL_SIZE
    }

    override suspend fun loadMergedInfoPostsAndFeed(): ReceiveChannel<ListItemEvent<List<PostModel>>> {
        return createMergedInfoPostsAndFeedChannel(mergedInfoPostsAndFeedPagination)
    }

    private suspend fun createMergedInfoPostsAndFeedChannel(pagination: PaginationController?): ReceiveChannel<ListItemEvent<List<PostModel>>> {
        val output = Channel<ListItemEvent<List<PostModel>>>(MERGED_FEED_CHANNEL_CAPACITY)

        var feedSuspended = true
        val feedBuffer: MutableList<ListItemEvent<List<PostModel>>> = ArrayList()

        val mutex = Mutex()

        suspend fun send(
            source: ReceiveChannel<*>?,
            event: ListItemEvent<List<PostModel>>
        ) {
            try {
                output.send(event)
            } catch (e: ClosedSendChannelException) {
                // Close the source channel too
                Timber.e(e)
                source?.cancel()
            }
        }

        val producers = coroutineContext.toCoroutineScope().launch {
            // Load the info posts.
            launchWorker {
                loadInfoPosts()
                    .takeUnless { it.isEmpty() }
                    ?.toList()
                    ?.also {
                        val event = ListItemEvent.Added<List<PostModel>>(it)
                        send(null, event)
                    }

                mutex.lock()

                feedSuspended = false
                feedBuffer.apply {
                    forEach {
                        send(null, it)
                    }
                    clear()
                }

                mutex.unlock()

                // Subscribe to info posts chanel with changes
                // handling.
                val infoPostsChannel = loadInfoPostsWithChangesHandling()
                infoPostsChannel.consumeEach {
                    val batch = when (it) {
                        is ListItemEvent.Added -> ListItemEvent.Added<PostModel>(it.item)
                        is ListItemEvent.Changed -> ListItemEvent.Changed<PostModel>(it.item)
                        is ListItemEvent.Moved -> ListItemEvent.Moved<PostModel>(it.item)
                        is ListItemEvent.Cleared -> ListItemEvent.Cleared()
                        is ListItemEvent.Removed -> it.previousChildName
                            ?.let { key -> ListItemEvent.Removed<PostModel>(key) }
                            ?: ListItemEvent.Removed<PostModel>(it.item)
                    }.toBatched()
                    send(infoPostsChannel, batch)
                }
            }

            launchWorker {
                val feedChannel = createFeedWithChangesHandlingChannel(pagination)
                feedChannel.consumeEach { event ->
                    // Start loading the feed and store it in a buffer
                    // until info-posts are loaded.
                    if (feedSuspended) try {
                        mutex.lock()

                        if (feedSuspended) {
                            feedBuffer += event
                            return@consumeEach
                        }
                    } finally {
                        mutex.unlock()
                    }

                    send(feedChannel, event)
                }
            }
        }

        // Close the producers job when the
        // channel dies.
        output.invokeOnClose {
            producers.cancel()
        }

        return output
    }

    private suspend fun createFeedWithChangesHandlingChannel(pagination: PaginationController?): ReceiveChannel<ListItemEvent<List<PostModel>>> {
        return postsRepository.loadFeedWithChangesHandling(pagination).withBuffer()
    }

    override suspend fun loadWallWithChangesHandling(accountId: String): ReceiveChannel<ListItemEvent<List<PostModel>>> {
        return postsRepository.loadWallWithChangesHandling(accountId).withBuffer()
    }

    override suspend fun loadInfoPosts(): List<InfoPostModel> = postsRepository.loadInfoPosts()
    override suspend fun loadInfoPostsWithChangesHandling(): ReceiveChannel<ListItemEvent<InfoPostModel>> = postsRepository.loadInfoPostsWithChangesHandling()
    override suspend fun loadInfoPost(postId: String): PostModel? = postsRepository.loadInfoPost(postId)
    override suspend fun loadById(id: String): ReceiveChannel<PostModel?> = postsRepository.loadById(id)
    override suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllByGroupId(groupId)
    override suspend fun loadAllByGroupIdImmediately(groupId: String): List<PostModel> = postsRepository.preloadGroupFeed(groupId)

    private val viewItemChannel = Channel<ListItemEvent<PostModel>>(100)

    init {
        GlobalScope.launchWorker {
            viewItemChannel.withBuffer(bufferWindow = SEND_VIEWED_ITEMS_BUFFER_DELAY).consumeEach {
                if (it.item.isNotEmpty()) {
                    try {
                        postsRepository.sendViewed(it.item.map { it.id })
                    } catch (e: Exception) {
                        Timber.d(e) //ignore exception here
                    }
                }
            }
        }
    }

    override suspend fun onItemViewed(item: PostModel) {
        if (item.author.id == userProfileInteractorImpl.getAccountIdOrException()) {
            return
        }
        viewItemChannel.send(ListItemEvent.Added(item))
    }

    override suspend fun onItemOpened(item: PostModel) = postsRepository.sendOpened(listOf(item.id))

    override suspend fun resetCounter() = postsRepository.resetCounter()

    override suspend fun createNeed(post: RawPostModel) {
        return postsRepository.createNeed(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)
        ))
    }

    override suspend fun updateNeed(post: RawPostModel) {
        postsRepository.updateNeed(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun changeStatus(id: String, status: ExpirationType) {
        postsRepository.changeStatus(id, status)
    }

    override suspend fun createGeneralPost(post: RawPostModel) {
        return postsRepository.createGeneralPost(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun updateGeneralPost(post: RawPostModel) {

        postsRepository.updateGeneralPost(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun createOffer(post: RawPostModel) {
        return postsRepository.createOffer(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun updateOffer(post: RawPostModel) {
        postsRepository.updateOffer(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun createUserRecommendation(post: RawRecommendPostModel) = postsRepository.createUserRecommendation(post)
    override suspend fun updateUserRecommendation(post: RawRecommendPostModel) = postsRepository.updateUserRecommendation(post)
    override suspend fun getShareOfferPostPrice(): Long? = postsRepository.getShareOfferPostPrice()
    override suspend fun getShareOfferPostPerUserPrice(): Long? = postsRepository.getShareOfferPostPerUserPrice()
    override suspend fun getPromotePostPrice(): Long = postsRepository.getPromotePostPrice() ?: 0L
    override suspend fun removePost(postId: String) = postsRepository.removePost(postId)
    override suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel =
            postsRepository.repostPost(postId, text, privacy)

    override suspend fun hideInfoPost(postId: String) = postsRepository.hideInfoPost(postId)
    override suspend fun loadOfferCategories(): List<OfferCategoryModel> = postsRepository.loadOfferCategories()
    override suspend fun promote(post: PostModel) = postsRepository.promote(post)

    private suspend fun processTags(post: RawPostModel): List<String> {
        val customTagsAndTagsWithIds: List<TagModel> = post.tags

        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags.map { it.toString() })
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

    private suspend fun processImages(post: RawPostModel): List<String> {
        return post.uploadedImages + post.imagesToUpload
            .map {
                GlobalScope.asyncWorker { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_POSTS)) }
            }
            .map {
                it.await()
            }
    }

    override suspend fun getDefaultExpirationDays(): Long = postsRepository.getDefaultExpirationDays()

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 2_000L

        private const val MERGED_FEED_INITIAL_SIZE = 100L
        private const val MERGED_FEED_CHANNEL_CAPACITY = 20
    }
}