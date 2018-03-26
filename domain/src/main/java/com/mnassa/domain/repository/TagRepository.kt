package com.mnassa.domain.repository

import com.mnassa.domain.model.TagModel

/**
 * Created by Peter on 2/22/2018.
 */
interface TagRepository {
    suspend fun search(searchKeyword: String): List<TagModel>
    suspend fun createCustomTagIds(tags: List<String>): List<String>
    suspend fun getTagsByIds(ids: List<String>): List<TagModel>
}