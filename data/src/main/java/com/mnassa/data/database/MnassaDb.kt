package com.mnassa.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mnassa.data.database.dao.UserPostJoinDao
import com.mnassa.data.database.entity.PostRoomEntity
import com.mnassa.data.database.entity.PostUserJoin
import com.mnassa.data.database.entity.UserRoomEntity

/**
 * Created by Peter on 7/11/2018.
 */
@Database(
        entities = [(PostRoomEntity::class),(UserRoomEntity::class),(PostUserJoin::class)],
        version = 4
)
@TypeConverters(DbConverters::class)
abstract class MnassaDb : RoomDatabase() {
    abstract fun getUserPostJoinDao(): UserPostJoinDao
}