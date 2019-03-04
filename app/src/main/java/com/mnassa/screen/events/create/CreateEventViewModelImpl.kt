package com.mnassa.screen.events.create

import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.LocationPlaceModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventViewModelImpl(private val eventId: String?,
                               private val eventsInteractor: EventsInteractor,
                               private val placeFinderInteractor: PlaceFinderInteractor,
                               private val tagInteractor: TagInteractor,
                               private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), CreateEventViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private val publichMutex = Mutex()

    override suspend fun publish(model: RawEventModel) {
        publichMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    if (model.id == null) {
                        eventsInteractor.createEvent(model)
                    } else {
                        eventsInteractor.editEvent(model)
                    }
                    closeScreenChannel.send(Unit)
                }
            }
        }
    }

    override suspend fun getTag(tagId: String): TagModel? = handleExceptionsSuspend { tagInteractor.get(tagId) }
    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)
    override suspend fun canPromoteEvents(): Boolean = handleExceptionsSuspend { userProfileInteractor.getPermissions().consume { receive() }.canPromoteEvent } ?: false
    override suspend fun getPromoteEventPrice(): Long = handleExceptionsSuspend { eventsInteractor.getPromotePostPrice() } ?: 0L
    override suspend fun getUserLocation(): LocationPlaceModel? = handleExceptionsSuspend { userProfileInteractor.getProfileById(userProfileInteractor.getAccountIdOrException())?.location }
}