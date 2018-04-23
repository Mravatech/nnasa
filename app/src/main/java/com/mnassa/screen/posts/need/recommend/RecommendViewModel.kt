package com.mnassa.screen.posts.need.recommend

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/27/2018.
 */
interface RecommendViewModel : MnassaViewModel {
    val connectionsChannel: BroadcastChannel<List<ShortAccountModel>>

    data class RecommendViewModelParams(
            val excludedAccounts: List<String>
    )
}