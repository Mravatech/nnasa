package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.repository.PostsRepository
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository) : PostsInteractor {

    override suspend fun loadAll(): ReceiveChannel<ListItemEvent<Post>> = postsRepository.loadAllWithChangesHandling()
    override suspend fun loadById(id: String): ReceiveChannel<Post> = postsRepository.loadById(id)

    private val viewedItemIdsBuffer = ConcurrentSkipListSet<String>()
    private val sentViewedItemIds = ConcurrentSkipListSet<String>()
    private val lastItemsSentTime = AtomicLong()
    private var sendViewedItemsJob: Job? = null

    override suspend fun onItemViewed(item: Post) {
        val id = item.id
        if (sentViewedItemIds.contains(id)) {
            return
        }

        //bufferize items to send
        viewedItemIdsBuffer.add(id)

        if (System.currentTimeMillis() - lastItemsSentTime.get() < SEND_VIEWED_ITEMS_BUFFER_DELAY) {
            sendViewedItemsJob?.cancel()
        }
        sendViewedItemsJob = launch {
            delay(SEND_VIEWED_ITEMS_BUFFER_DELAY)

            val itemsToSend = viewedItemIdsBuffer.toList()
            viewedItemIdsBuffer.removeAll(itemsToSend)
            try {
                postsRepository.sendViewed(itemsToSend)
                sentViewedItemIds.addAll(itemsToSend)
                lastItemsSentTime.set(System.currentTimeMillis())
            } catch (e: Exception) {
                //ignore all exceptions here
                Timber.d(e)
                viewedItemIdsBuffer.addAll(itemsToSend)
            }
        }
    }

    override suspend fun createNeed(text: String, images: List<File>, privacyType: PostPrivacyType, privacyConnections: List<String>): Post {
        //todo: upload images to filestore

        return postsRepository.createNeed(text, emptyList(), privacyType, privacyConnections)
    }

    override suspend fun removePost(postId: String) {
        postsRepository.removePost(postId)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}