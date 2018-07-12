package com.mnassa.data.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.mnassa.data.database.DbConverters
import com.mnassa.domain.model.PostModel

/**
 * Created by Peter on 7/11/2018.
 */
@Entity(tableName = PostRoomEntity.TABLE_NAME,
        indices = [(Index("accountId"))])
class PostRoomEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: String,
        @ColumnInfo(name = "accountId")
        val accountId: String,
        @ColumnInfo(name = "data")
        val data: String
) {

    constructor(post: PostModel, accountId: String) : this(post.id, accountId, DbConverters.toString(post))

    fun toPostModel(): PostModel = DbConverters.fromString(data)

    companion object {
        const val TABLE_NAME = "POST"
    }
}