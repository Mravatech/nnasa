package com.mnassa.domain.repository

import com.mnassa.domain.model.TagModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/22/2018.
 */
interface TagRepository {
    fun load(): ReceiveChannel<TagModel>
    suspend fun get(id: String): TagModel?
}