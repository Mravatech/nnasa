package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.repository.EventsRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 * Created by Peter on 4/13/2018.
 */
class EventsInteractorImpl(
        private val eventsRepository: EventsRepository,
        private val userProfileInteractor: UserProfileInteractor,
        private val storageInteractor: StorageInteractor,
        private val preferencesInteractor: PreferencesInteractor,
        private val tagInteractor: TagInteractor) : EventsInteractor {

    private val viewItemChannel = Channel<ListItemEvent<EventModel>>(10)

    override val eventsOutOfTimeUpperBoundCounter = BroadcastChannel<Int>(Channel.CONFLATED)

    override val eventsTimeUpperBound = BroadcastChannel<Date>(Channel.CONFLATED)

    init {
        GlobalScope.launchWorker {
            val newItemsSavedTime = preferencesInteractor
                .getLong(KEY_EVENTS_TIME_UPPER_BOUND, Date().time)
                .let(::Date)
            eventsTimeUpperBound.apply {
                send(newItemsSavedTime)
                consumeEach {
                    preferencesInteractor.saveLong(KEY_EVENTS_TIME_UPPER_BOUND, it.time)
                }
            }
        }
        GlobalScope.launchWorker {
            viewItemChannel.withBuffer(bufferWindow = SEND_VIEWED_ITEMS_BUFFER_DELAY).consumeEach {
                if (it.item.isNotEmpty()) {
                    try {
                        eventsRepository.sendViewed(it.item.map { it.id })
                    } catch (e: Exception) {
                        Timber.d(e) //ignore exception here
                    }
                }
            }
        }
    }

    override val eventsPagination = PaginationController(EVENTS_INITIAL_SIZE)

    override suspend fun onItemViewed(item: EventModel) {
        if (item.author.id == userProfileInteractor.getAccountIdOrException()) {
            return
        }
        viewItemChannel.send(ListItemEvent.Added(item))
    }

    override suspend fun onItemOpened(item: EventModel) = eventsRepository.sendOpened(listOf(item.id))

    override suspend fun resetCounter() = eventsRepository.resetCounter()

    override suspend fun createEvent(model: RawEventModel) {
        val allImages = model.uploadedImages +
                coroutineScope {
                    model.imagesToUpload
                        .map {
                            asyncWorker {
                                storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_EVENTS))
                            }
                        }
                        .map {
                            it.await()
                        }
                }
        model.uploadedImages.clear()
        model.uploadedImages.addAll(allImages)
        model.tagIds.clear()
        model.tagIds.addAll(createTags(model.tagModels))

        return eventsRepository.createEvent(model)
    }

    override suspend fun editEvent(model: RawEventModel) {
        val allImages = model.uploadedImages +
                coroutineScope {
                    model.imagesToUpload
                        .map {
                            asyncWorker { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_EVENTS)) }
                        }
                        .map {
                            it.await()
                        }
                }
        model.uploadedImages.clear()
        model.uploadedImages.addAll(allImages)
        model.tagIds.clear()
        model.tagIds.addAll(createTags(model.tagModels))

        return eventsRepository.editEvent(model)
    }

    override suspend fun changeStatus(event: EventModel, status: EventStatus) {
        val groupIds = event.groups.map { it.id }.toSet()
        val model = RawEventModel(
                id = event.id,
                status = status,
                tagModels = emptyList(),
                tagIds = event.tags.toMutableSet(),
                locationType = event.locationType,
                type = event.type,
                ticketsPerAccount = event.ticketsPerAccount.toInt(),
                ticketsTotal = event.ticketsTotal.toInt(),
                price = event.price.takeIf { it > 0 },
                title = event.title,
                privacy = PostPrivacyOptions(event.privacyType, event.privacyConnections, groupIds),
                uploadedImages = event.pictures.toMutableSet(),
                imagesToUpload = emptyList(),
                durationMillis = event.duration?.toMillis() ?: 0L,
                startDateTime = event.startAt,
                description = event.text,
                groupIds = groupIds,
                needPush = null
        )

        return eventsRepository.editEvent(model)
    }

    override suspend fun promote(id: String) {
        eventsRepository.promote(id)
    }

    override suspend fun getPromotePostPrice(): Long {
        return eventsRepository.getPromoteEventPrice() ?: 0L
    }

    private suspend fun createTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
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

    override suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<List<EventModel>>> {
        val output = Channel<ListItemEvent<List<EventModel>>>(EVENTS_CHANNEL_CAPACITY)

        var feedBarrierMaxTime = eventsTimeUpperBound.openSubscription().receive()
        val feedBarrierMap = HashMap<String, EventModel>()
        val feedBarrierMutex = Mutex()

        suspend fun send(
            source: ReceiveChannel<*>?,
            event: ListItemEvent<List<EventModel>>
        ) {
            try {
                output.send(event)
            } catch (e: ClosedSendChannelException) {
                // Close the source channel too
                Timber.e(e)
                source?.cancel()
            }
        }

        suspend fun sendEvent(
            source: ReceiveChannel<*>?,
            event: ListItemEvent<List<EventModel>>
        ) {
            var pendingPostsSize = 0
            when (event) {
                is ListItemEvent.Added<*>,
                is ListItemEvent.Moved<*>,
                is ListItemEvent.Changed<*> -> {
                    val postsToSendNow = ArrayList<EventModel>()
                    feedBarrierMutex.withLock {
                        for (item in event.item) {
                            if (item.createdAt > feedBarrierMaxTime) {
                                feedBarrierMap[item.id] = item
                            } else {
                                // We will send this post
                                // immediately.
                                postsToSendNow.add(item)
                            }
                        }

                        pendingPostsSize = feedBarrierMap.size
                    }

                    val eventDecomposed = ListItemEvent.Added(postsToSendNow.toList())
                    send(source, eventDecomposed)
                }
                is ListItemEvent.Removed<List<EventModel>> -> {
                    feedBarrierMutex.withLock {
                        //                        for (item in event.item) feedBarrierMap.remove(item.id)

                        pendingPostsSize = feedBarrierMap.size
                    }

                    send(source, event)
                }
            }

            eventsOutOfTimeUpperBoundCounter.send(pendingPostsSize)
        }

        val producers = coroutineContext.toCoroutineScope().launch {
            launchWorker {
                try {
                    sendEvent(null, ListItemEvent.Added(loadAllImmediately()))
                } catch (e: Exception) {
                    Timber.e(e) //ignore exception here
                }

                val eventsChannel = eventsRepository.getEventsFeedChannel(eventsPagination).withBuffer()
                eventsChannel.consumeEach { event ->
                    sendEvent(eventsChannel, event)
                }
            }

            launchWorker {
                eventsTimeUpperBound.consumeEach { date ->
                    feedBarrierMutex.withLock {
                        feedBarrierMaxTime = date

                        // Send new events right
                        // now.
                        val eventsToSendNow = ArrayList<EventModel>()

                        val keys = feedBarrierMap.keys.toSet()
                        for (key in keys) {
                            val event = feedBarrierMap[key]!!
                            if (event.createdAt <= date) {
                                eventsToSendNow.add(event)
                                feedBarrierMap.remove(key)
                            }
                        }

                        val event = ListItemEvent.Added(eventsToSendNow.toList())
                        send(null, event)

                        // Update the size of a list
                        // of pending posts
                        val pendingPostsSize = feedBarrierMap.size
                        eventsOutOfTimeUpperBoundCounter.send(pendingPostsSize)
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

    override suspend fun loadAllImmediately(): List<EventModel> = eventsRepository.preloadEvents()
    override suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<EventModel>> = eventsRepository.loadAllByGroupId(groupId)
    override suspend fun loadAllByGroupIdImmediately(groupId: String): List<EventModel> = eventsRepository.loadAllByGroupIdImmediately(groupId)
    override suspend fun loadByIdChannel(eventId: String): ReceiveChannel<EventModel?> = eventsRepository.getEventsChannel(eventId)
    override suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>> = eventsRepository.getTicketsChannel(eventId)
    override suspend fun getTickets(eventId: String): List<EventTicketModel> = eventsRepository.getTickets(eventId)

    override suspend fun getBoughtTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>> {
        return getTicketsChannel(eventId)
            .map { list ->
                val accountId = userProfileInteractor.getAccountIdOrException()
                return@map list
                    .filter { it.ownerId == accountId }
                    .toList()
            }
    }

    override suspend fun getBoughtTicketsCount(eventId: String): Long {
        val userId = userProfileInteractor.getAccountIdOrNull() ?: return 0L
        var counter = 0L
        getTickets(eventId).forEach {
            if (it.ownerId == userId) {
                counter += it.ticketCount
            }
        }
        return counter
    }

    override suspend fun buyTickets(eventId: String, ticketsCount: Long) = eventsRepository.buyTickets(eventId, ticketsCount)
    override suspend fun getAttendedUsers(eventId: String): List<EventAttendee> = eventsRepository.getAttendedUsers(eventId)
    override suspend fun getAttendedUsersChannel(eventId: String): ReceiveChannel<List<EventAttendee>> = eventsRepository.getAttendedUsersChannel(eventId)
    override suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>) = eventsRepository.saveAttendedUsers(eventId, presentUsers, notPresentUsers)

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L

        private const val EVENTS_INITIAL_SIZE = 100L
        private const val EVENTS_CHANNEL_CAPACITY = 20

        private const val KEY_EVENTS_TIME_UPPER_BOUND = "events::events_time_upper_bound"
    }
}