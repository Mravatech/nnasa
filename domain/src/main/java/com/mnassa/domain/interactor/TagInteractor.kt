package com.mnassa.domain.interactor

import com.mnassa.domain.model.TagModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/7/2018
 */

interface TagInteractor{
    suspend fun search(searchKeyword: String): List<TagModel>
    suspend fun createCustomTagIds(tags: List<String>): List<String>
    suspend fun getTagsByIds(ids: List<String>): List<TagModel>
}
