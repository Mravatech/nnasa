package com.mnassa.screen.home

import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface HomeViewModel : MnassaViewModel {
    val unreadEventsCountChannel: BroadcastChannel<Int>
    val unreadNeedsCountChannel: BroadcastChannel<Int>
    val permissionsChannel: BroadcastChannel<PermissionsModel>

    val showAddTagsDialog: BroadcastChannel<Unit>
    suspend fun getInterests(): List<TagModel>
    suspend fun getOffers(): List<TagModel>
    suspend fun getProfile(): ProfileAccountModel?
}