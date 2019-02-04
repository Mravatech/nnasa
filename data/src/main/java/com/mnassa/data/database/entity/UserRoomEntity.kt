package com.mnassa.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = UserRoomEntity.TABLE_NAME,
        indices = [(Index("id"))])
class UserRoomEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: String
) {
    companion object {
        const val TABLE_NAME = "USER"
    }
}