package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.network.bean.firebase.TagBean
import com.mnassa.domain.models.TagModel
import com.mnassa.domain.repository.TagRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 2/22/2018.
 */
class TagRepositoryImpl(private val converter: ConvertersContext, private val databaseReference: DatabaseReference) : TagRepository {

    override fun load(): ReceiveChannel<TagModel> {
        return load<TagBean>(databaseReference, TABLE_NAME).map { converter.convert<TagModel>(it) }
    }

    override suspend fun get(id: String): TagModel? {
        val tagBean: TagBean? = get(databaseReference, TABLE_NAME, id)
        return tagBean?.run { converter.convert(this) }
    }

    companion object {
        private const val TABLE_NAME = "tags"
    }
}