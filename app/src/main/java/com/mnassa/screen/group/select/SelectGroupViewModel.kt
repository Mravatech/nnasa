package com.mnassa.screen.group.select

import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/23/2018.
 */
interface SelectGroupViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<List<GroupModel>>

    data class Params(val adminOnly: Boolean)
}