package com.mnassa.domain.repository

import com.mnassa.domain.model.TagModel

/**
 * Created by Peter on 2/22/2018.
 */
interface TagRepository {
    suspend fun search(search: String): List<TagModel>
}