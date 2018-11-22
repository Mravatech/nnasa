package com.mnassa.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
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