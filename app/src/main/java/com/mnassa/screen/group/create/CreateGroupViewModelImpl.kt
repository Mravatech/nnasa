package com.mnassa.screen.group.create

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.RawGroupModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

/**
 * Created by Peter on 5/22/2018.
 */
class CreateGroupViewModelImpl(private val groupId: String?,
                               private val groupsInteractor: GroupsInteractor,
                               private val placeFinderInteractor: PlaceFinderInteractor,
                               private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), CreateGroupViewModel {

    override val closeScreenChanel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    private val applyChangesMutex = Mutex()

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)

    override suspend fun applyChanges(group: RawGroupModel) {
        applyChangesMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    if (groupId == null) {
                        groupsInteractor.createGroup(group)
                    } else groupsInteractor.updateGroup(group)
                }
                closeScreenChanel.send(Unit)
            }
        }
    }

    override suspend fun getTag(tagId: String): TagModel? = tagInteractor.get(tagId)
}