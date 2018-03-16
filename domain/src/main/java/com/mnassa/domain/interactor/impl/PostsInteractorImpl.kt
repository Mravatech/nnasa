package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.repository.PostsRepository
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.delay
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository) : PostsInteractor {

    override suspend fun loadAll(): ReceiveChannel<ListItemEvent<Post>> = postsRepository.loadAll()
    override suspend fun loadById(id: String): Post? = postsRepository.loadById(id)

    private val viewedItemIdsBuffer = ConcurrentSkipListSet<String>()
    private val sentViewedItemIds = ConcurrentSkipListSet<String>()
    private val lastItemsSentTime = AtomicLong()
    private var sendViewedItemsJob: Job? = null

    override fun onItemViewed(item: Post) {
        val id = item.id
        if (sentViewedItemIds.contains(id)) {
            return
        }

        //bufferize items to send

        viewedItemIdsBuffer.add(id)

        if (System.currentTimeMillis() - lastItemsSentTime.get() < SEND_VIEWED_ITEMS_BUFFER_DELAY) {
            sendViewedItemsJob?.cancel()
        }
        sendViewedItemsJob = async {
            delay(SEND_VIEWED_ITEMS_BUFFER_DELAY)

            val itemsToSend = viewedItemIdsBuffer.toList()
            postsRepository.sendViewed(itemsToSend)
            viewedItemIdsBuffer.removeAll(itemsToSend)
            sentViewedItemIds.addAll(itemsToSend)
            lastItemsSentTime.set(System.currentTimeMillis())
        }
    }

    override suspend fun createNeed(text: String, images: List<File>, privacyType: PostPrivacyType, privacyConnections: List<String>): Post {
        //todo: upload images to filestore

        return postsRepository.createNeed(text, emptyList(), privacyType, privacyConnections)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}