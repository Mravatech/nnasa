package com.mnassa.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mnassa.data.database.DbConverters
import com.mnassa.domain.model.PostModel
import java.io.InvalidClassException

/**
 * Created by Peter on 7/11/2018.
 */
@Entity(tableName = PostRoomEntity.TABLE_NAME,
        indices = [(Index("createdAt")), (Index("id", unique = true))])
class PostRoomEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: String,
        @ColumnInfo(name = "createdAt")
        val createdAt: Long,
        @ColumnInfo(name = "data")
        val data: String
) {

    constructor(post: PostModel) : this(post.id, post.createdAt.time, DbConverters.toString(post))

    fun toPostModel(): PostModel? = try { DbConverters.fromString(data) } catch (e: InvalidClassException) { null }

    companion object {
        const val TABLE_NAME = "POST"
    }
}