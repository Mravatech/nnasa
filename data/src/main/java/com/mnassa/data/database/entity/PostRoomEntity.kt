package com.mnassa.data.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
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
        @ColumnInfo(name = "userId")
        val userId: String?,
        @ColumnInfo(name = "createdAt")
        val createdAt: Long,
        @ColumnInfo(name = "data")
        val data: String
) {

    constructor(userId: String?, post: PostModel) : this(post.id, userId, post.createdAt.time, DbConverters.toString(post))

    fun toPostModel(): PostModel? = try { DbConverters.fromString(data) } catch (e: InvalidClassException) { null }

    companion object {
        const val TABLE_NAME = "POST"
    }
}