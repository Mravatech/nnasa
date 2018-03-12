package com.mnassa.domain.interactor

import com.mnassa.domain.model.TagModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/7/2018
 */

interface TagInteractor{
    suspend fun search(search: String): List<TagModel>
}
