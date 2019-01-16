package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.domain.repository.EventsRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import timber.log.Timber

/**
 * Created by Peter on 4/13/2018.
 */
class EventsInteractorImpl(
        private val eventsRepository: EventsRepository,
        private val userProfileInteractor: UserProfileInteractor,
        private val storageInteractor: StorageInteractor,
        private val tagInteractor: TagInteractor) : EventsInteractor {

    private val viewItemChannel = ArrayChannel<ListItemEvent<EventModel>>(10)

    init {
        launchWorker {
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
        val allImages = model.uploadedImages + model.imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_EVENTS)) }
        }.map { it.await() }
        model.uploadedImages.clear()
        model.uploadedImages.addAll(allImages)
        model.tagIds.clear()
        model.tagIds.addAll(createTags(model.tagModels))

        return eventsRepository.createEvent(model)
    }

    override suspend fun editEvent(model: RawEventModel) {
        val allImages = model.uploadedImages + model.imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_EVENTS)) }
        }.map { it.await() }
        model.uploadedImages.clear()
        model.uploadedImages.addAll(allImages)
        model.tagIds.clear()
        model.tagIds.addAll(createTags(model.tagModels))

        return eventsRepository.editEvent(model)
    }

    override suspend fun changeStatus(event: EventModel, status: EventStatus) {

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
                privacy = PostPrivacyOptions(event.privacyType, event.privacyConnections),
                uploadedImages = event.pictures.toMutableSet(),
                imagesToUpload = emptyList(),
                durationMillis = event.duration?.toMillis() ?: 0L,
                startDateTime = event.startAt,
                description = event.text,
                groupIds = event.groups.map { it.id }.toSet(),
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
        return produce {
            try {
                send(ListItemEvent.Added(loadAllImmediately()))
            } catch (e: Exception) {
                Timber.e(e) //ignore exception here
            }
            eventsRepository.getEventsFeedChannel(eventsPagination).withBuffer().consumeEach { send(it) }
        }
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
    }
}