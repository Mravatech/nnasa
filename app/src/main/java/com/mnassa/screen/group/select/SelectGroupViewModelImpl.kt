package com.mnassa.screen.group.select

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.isAdmin
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 5/23/2018.
 */
class SelectGroupViewModelImpl(private val params: SelectGroupViewModel.Params,
                               private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), SelectGroupViewModel {
    override val groupChannel: BroadcastChannel<List<GroupModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            groupsInteractor.getMyGroups().consumeEach {
                val result = if (params.adminOnly) it.filter { it.isAdmin } else it
                groupChannel.send(result)
            }
        }
    }
}