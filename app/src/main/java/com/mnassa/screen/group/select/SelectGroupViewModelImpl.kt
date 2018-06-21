package com.mnassa.screen.group.select

import android.os.Bundle
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.isAdmin
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 5/23/2018.
 */
class SelectGroupViewModelImpl(private val params: SelectGroupViewModel.Params,
                               private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), SelectGroupViewModel {
    override val groupChannel: BroadcastChannel<List<GroupModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getMyGroups().consumeEach {
                val result = if (params.adminOnly) it.filter { it.isAdmin } else it
                groupChannel.send(result)
            }
        }
    }
}