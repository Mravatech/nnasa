package com.mnassa.domain.repository

import com.mnassa.domain.model.NewsFeedItemModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/13/2018.
 */
interface NewsFeedRepository {
    suspend fun loadAll(): ReceiveChannel<NewsFeedItemModel>
    suspend fun loadById(id: String): NewsFeedItemModel?
}