package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.EventsRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
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
        launch {
            viewItemChannel.bufferize(SubscriptionsContainerDelegate(), SEND_VIEWED_ITEMS_BUFFER_DELAY).consumeEach {
                if (it.item.isNotEmpty()) {
                    try {
                        eventsRepository.sendViewed(it.item.map { it.id })
                    } catch (e: Exception) {
                        Timber.d(e)
                    }
                }
            }
        }
    }

    override suspend fun onItemViewed(item: EventModel) {
        if (item.author.id == userProfileInteractor.getAccountIdOrException()) {
            return
        }
        viewItemChannel.send(ListItemEvent.Added(item))
    }

    override suspend fun createEvent(model: CreateOrEditEventModel) {
        val allImages = model.uploadedImages + model.imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_EVENTS)) }
        }.map { it.await() }
        model.uploadedImages.clear()
        model.uploadedImages.addAll(allImages)
        model.tagIds.clear()
        model.tagIds.addAll(createTags(model.tagModels))

        return eventsRepository.createEvent(model)
    }

    override suspend fun editEvent(model: CreateOrEditEventModel) {
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
        val model = CreateOrEditEventModel(
                id = event.id,
                status = status,
                tagModels = emptyList(),
                tagIds = event.tags.toMutableSet(),
                locationType = event.locationType,
                type = event.type,
                ticketsPerAccount = event.ticketsPerAccount.toInt(),
                ticketsTotal = event.ticketsTotal.toInt(),
                price = event.price.takeIf { it > 0 },
                locationDescription = "",
                title = event.title,
                privacy = PostPrivacyOptions(event.privacyType, event.privacyConnections),
                uploadedImages = event.pictures.toMutableSet(),
                imagesToUpload = emptyList(),
                durationMillis = event.duration?.toMillis() ?: 0L,
                startDateTime = event.startAt,
                description = event.text
        )

        return eventsRepository.editEvent(model)
    }

    private suspend fun createTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags)
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

    override suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>> {
        return eventsRepository.getEventsFeedChannel()
    }

    override suspend fun loadByIdChannel(eventId: String): ReceiveChannel<EventModel?> {
        return eventsRepository.getEventsChannel(eventId)
    }

    override suspend fun getTicketsChannel(eventId: String): ReceiveChannel<List<EventTicketModel>> {
        return eventsRepository.getTicketsChannel(eventId)
    }

    override suspend fun getTickets(eventId: String): List<EventTicketModel> {
        return eventsRepository.getTickets(eventId)
    }

    override suspend fun canBuyTicket(eventId: String): Boolean {
        val event = loadByIdChannel(eventId).consume { receive() } ?: return false
        if (event.ticketsSold >= event.ticketsTotal || event.status != EventStatus.OPENED) return false
        return getBoughtTicketsCount(eventId) < event.ticketsPerAccount
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

    override suspend fun buyTickets(eventId: String, ticketsCount: Long) {
        eventsRepository.buyTickets(eventId, ticketsCount)
    }

    override suspend fun getAttendedUsers(eventId: String): List<EventAttendee> {
        return eventsRepository.getAttendedUsers(eventId)
    }

    override suspend fun saveAttendedUsers(eventId: String, presentUsers: List<String>, notPresentUsers: List<String>) {
        eventsRepository.saveAttendedUsers(eventId, presentUsers, notPresentUsers)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}