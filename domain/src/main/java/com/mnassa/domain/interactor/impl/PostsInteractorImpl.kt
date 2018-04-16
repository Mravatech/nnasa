package com.mnassa.domain.interactor.impl

import android.net.Uri
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.PostsRepository
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository,
                          private val storageInteractor: StorageInteractor,
                          private val userProfileInteractorImpl: UserProfileInteractor) : PostsInteractor {

    override suspend fun loadAll(): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllWithChangesHandling()
    override suspend fun loadById(id: String): ReceiveChannel<PostModel> = postsRepository.loadById(id)
    override suspend fun loadAllUserPostByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllByAccountUd(accountId)

    private val viewedItemIdsBuffer = Collections.synchronizedSet(HashSet<String>())
    private val sentViewedItemIds = Collections.synchronizedSet(HashSet<String>())
    private val lastItemsSentTime = AtomicLong()
    private var sendViewedItemsJob: Job? = null

    override suspend fun onItemViewed(item: PostModel) {
        val id = item.id
        if (item.author.id == userProfileInteractorImpl.getAccountId() || sentViewedItemIds.contains(id)) {
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
                if (itemsToSend.isNotEmpty()) {
                    postsRepository.sendViewed(itemsToSend)
                    sentViewedItemIds.addAll(itemsToSend)
                }
                lastItemsSentTime.set(System.currentTimeMillis())
            } catch (e: Exception) {
                //ignore all exceptions here
                Timber.d(e)
                viewedItemIdsBuffer.addAll(itemsToSend)
            }
        }
    }

    override suspend fun createNeed(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacyType: PostPrivacyType,
            toAll: Boolean,
            privacyConnections: List<String>
    ): PostModel {
        val allImages = uploadedImages + imagesToUpload.map {
            async { storageInteractor.sendAvatar(StoragePhotoDataImpl(it, FOLDER_AVATARS)) }
        }.map { it.await() }
        return postsRepository.createNeed(text, allImages, privacyType, toAll, privacyConnections)
    }

    override suspend fun updateNeed(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacyType: PostPrivacyType,
            toAll: Boolean,
            privacyConnections: List<String>) {
        val allImages = uploadedImages + imagesToUpload.map {
            async { storageInteractor.sendAvatar(StoragePhotoDataImpl(it, FOLDER_AVATARS)) }
        }.map { it.await() }
        postsRepository.updateNeed(postId, text, allImages, privacyType, toAll, privacyConnections)
    }

    override suspend fun removePost(postId: String) {
        postsRepository.removePost(postId)
    }

    override suspend fun repostPost(postId: String, text: String?, privacyConnections: List<String>): PostModel {
        return postsRepository.repostPost(postId, text, privacyConnections)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}