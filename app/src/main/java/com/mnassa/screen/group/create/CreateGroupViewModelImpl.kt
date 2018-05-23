package com.mnassa.screen.group.create

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.RawGroupModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/22/2018.
 */
class CreateGroupViewModelImpl(private val groupId: String?,
                               private val groupsInteractor: GroupsInteractor,
                               private val placeFinderInteractor: PlaceFinderInteractor) : MnassaViewModelImpl(), CreateGroupViewModel {

    override val closeScreenChanel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)

    override fun applyChanges(group: RawGroupModel) {
        handleException {
            withProgressSuspend {
                if (groupId == null) {
                    groupsInteractor.createGroup(group)
                } else groupsInteractor.updateGroup(group)
            }
            closeScreenChanel.send(Unit)
        }
    }
}