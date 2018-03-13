package com.mnassa.domain.repository

/**
 * Created by Peter on 3/13/2018.
 */
interface NewsFeedRepository {
    suspend fun loadAll()
    suspend fun loadById(id: String)
}