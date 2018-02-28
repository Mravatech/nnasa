package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/22/2018.
 */
class TagRepositoryImpl(private val converter: ConvertersContext, private val databaseReference: DatabaseReference) : TagRepository {

    override fun load(): ReceiveChannel<TagModel> {
        TODO()
    }

    override suspend fun get(id: String): TagModel? {
        TODO()
    }

    companion object {
        private const val TABLE_NAME = "tags"
    }
}