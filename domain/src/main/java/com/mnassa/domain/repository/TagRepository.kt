package com.mnassa.domain.repository

import com.mnassa.domain.model.TagModel
import java.util.*

/**
 * Created by Peter on 2/22/2018.
 */
interface TagRepository {
    suspend fun get(id: String): TagModel?
    suspend fun getAll(): List<TagModel>
    suspend fun search(searchKeyword: String): List<TagModel>
    suspend fun createCustomTagIds(tags: List<String>): List<String>
    suspend fun getTagsByIds(ids: List<String>): List<TagModel>

    suspend fun getAddTagsDialogInterval(): Long?
    suspend fun getAddTagsDialogLastShowingTime(): Date?
    suspend fun setAddTagsDialogShowingTime(time: Date)
}