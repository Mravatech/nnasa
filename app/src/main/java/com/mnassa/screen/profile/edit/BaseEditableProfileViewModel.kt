package com.mnassa.screen.profile.edit

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 6/20/2018.
 */
interface BaseEditableProfileViewModel : MnassaViewModel {
    val addTagRewardChannel: BroadcastChannel<Long?>
    suspend fun calculateDeleteTagsPrice(tagsCount: Int): Long?

    suspend fun isInterestsMandatory(): Boolean
    suspend fun isOffersMandatory(): Boolean
}