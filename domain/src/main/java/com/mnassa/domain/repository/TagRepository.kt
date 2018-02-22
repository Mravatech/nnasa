package com.mnassa.domain.repository

import com.mnassa.domain.models.TagModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/22/2018.
 */
interface TagRepository {
    fun load(): ReceiveChannel<TagModel>
}