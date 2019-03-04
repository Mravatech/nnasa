package com.mnassa.screen.posts.need.sharing

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 3/21/2018.
 */
interface SharingOptionsViewModel : MnassaViewModel {
    val allConnections: ReceiveChannel<List<ShortAccountModel>>

    data class SharingOptionsParams(
            val excludedAccounts: Set<String>
    )
}