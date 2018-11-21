package com.mnassa.data.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

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