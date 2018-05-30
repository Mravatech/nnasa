package com.mnassa.screen.group.permissions

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by Peter on 5/30/2018.
 */
class GroupPermissionsViewModelImpl(
        private val groupId: String,
        private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupPermissionsViewModel {
}