package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.aggregator.AggregatorInEvent
import com.mnassa.domain.aggregator.AggregatorLive
import com.mnassa.domain.extensions.produceAccountChangedEvents
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository,
                          private val userRepository: UserRepository,
                          private val storageInteractor: StorageInteractor,
                          private val preferencesInteractor: PreferencesInteractor,
                          private val tagInteractor: TagInteractor,
                          private val userProfileInteractorImpl: UserProfileInteractor) : PostsInteractor {

    override val mergedInfoPostsAndFeedPagination = PaginationController(MERGED_FEED_INITIAL_SIZE)

    override var mergedInfoPostsAndFeedLiveTimeUpperBound: Date =
        preferencesInteractor
            .getLong(KEY_FEED_TIME_UPPER_BOUND, Date().time)
            .let {
                Date(it)
            }
        set(value) {
            if (value < field) {
                return
            }

            field = value

            GlobalScope.launchWorker {
                mergedInfoPostsAndFeedLive.revalidate()
            }

            // Save to shared
            // preferences
            preferencesInteractor.saveLong(KEY_FEED_TIME_UPPER_BOUND, value.time)
        }

    override val mergedInfoPostsAndFeedLive: AggregatorLive<PostModel> = AggregatorLive(
        source = { createMergedInfoPostsAndFeedChannel(mergedInfoPostsAndFeedPagination) },
        reconsume = { userProfileInteractorImpl.produceAccountChangedEvents() },
        comparator = compareBy { it.createdAt },
        isValid = { it.createdAt <= mergedInfoPostsAndFeedLiveTimeUpperBound || it is InfoPostModel }
    )

    private suspend fun createMergedInfoPostsAndFeedChannel(pagination: PaginationController?): ReceiveChannel<AggregatorInEvent<out PostModel>> {
        val output = Channel<AggregatorInEvent<out PostModel>>(MERGED_FEED_CHANNEL_CAPACITY)

        suspend fun send(
            source: ReceiveChannel<*>?,
            event: AggregatorInEvent<out PostModel>
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
            val firstInfoPosts = AtomicBoolean(true)
            val firstFeed = AtomicBoolean(true)

            val feedInitBuffer = ArrayList<AggregatorInEvent.Put<out PostModel>>()

            val mutex = Mutex()

            launchWorker {
                val infoPostsChannel = loadInfoPostsWithChangesHandling()
                infoPostsChannel.consumeEach { event ->
                    if (firstInfoPosts.getAndSet(false)) mutex.withLock {
                        val shouldSendInitEvent = !firstFeed.get()
                        val initEvent = event as AggregatorInEvent.Init<InfoPostModel>
                        feedInitBuffer.addAll(initEvent.events)

                        if (shouldSendInitEvent) {
                            send(infoPostsChannel, AggregatorInEvent.Init(feedInitBuffer.toList()))
                            feedInitBuffer.clear()
                        }
                    } else {
                        send(infoPostsChannel, event)
                    }
                }
            }

            launchWorker {
                val feedChannel = createFeedWithChangesHandlingChannel(pagination)
                feedChannel.consumeEach { event ->
                    if (firstFeed.getAndSet(false)) mutex.withLock {
                        val shouldSendInitEvent = !firstInfoPosts.get()
                        val initEvent = event as AggregatorInEvent.Init<PostModel>
                        feedInitBuffer.addAll(initEvent.events)

                        if (shouldSendInitEvent) {
                            send(feedChannel, AggregatorInEvent.Init(feedInitBuffer.toList()))
                            feedInitBuffer.clear()
                        }
                    } else {
                        send(feedChannel, event)
                    }
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

    private suspend fun createFeedWithChangesHandlingChannel(pagination: PaginationController?): ReceiveChannel<AggregatorInEvent<PostModel>> {
        return postsRepository.loadFeedWithChangesHandling(pagination)
    }

    override suspend fun loadWallWithChangesHandling(accountId: String, pagination: PaginationController?): ReceiveChannel<ListItemEvent<List<PostModel>>> {
        return postsRepository.loadWallWithChangesHandling(accountId, pagination).withBuffer()
    }

    override suspend fun loadInfoPosts(): List<InfoPostModel> = postsRepository.loadInfoPosts()
    override suspend fun loadInfoPostsWithChangesHandling(): ReceiveChannel<AggregatorInEvent<InfoPostModel>> = postsRepository.loadInfoPostsWithChangesHandling()
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
    override suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions) =
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

        private const val MERGED_FEED_INITIAL_SIZE = 20L
        private const val MERGED_FEED_CHANNEL_CAPACITY = 20

        private const val KEY_FEED_TIME_UPPER_BOUND = "posts::feed_time_upper_bound"
    }
}