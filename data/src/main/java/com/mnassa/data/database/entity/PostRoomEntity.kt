package com.mnassa.data.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Peter on 7/11/2018.
 */
@Entity(tableName = PostRoomEntity.TABLE_NAME)
class PostRoomEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        private val id: String
) {
        companion object {
            const val TABLE_NAME = "POST"
        }
}