package com.mnassa.screen.posts.profile.details

import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.posts.need.details.NeedDetailsViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/10/2018.
 */
interface RecommendedProfileViewModel : NeedDetailsViewModel {
    val connectionStatusChannel: BroadcastChannel<ConnectionStatus>
    fun connect(account: ShortAccountModel)
}