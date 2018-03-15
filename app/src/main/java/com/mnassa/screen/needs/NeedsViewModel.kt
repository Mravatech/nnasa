package com.mnassa.screen.needs

import com.mnassa.domain.model.NewsFeedItemModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/6/2018.
 */
interface NeedsViewModel : MnassaViewModel {
    suspend fun getNewsFeedChannel(): ReceiveChannel<NewsFeedItemModel>
}