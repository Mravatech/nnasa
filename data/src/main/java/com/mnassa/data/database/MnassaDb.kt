package com.mnassa.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.mnassa.data.database.dao.PostDao
import com.mnassa.data.database.entity.PostRoomEntity

/**
 * Created by Peter on 7/11/2018.
 */
@Database(
        entities = [(PostRoomEntity::class)],
        version = 2
)
@TypeConverters(DbConverters::class)
abstract class MnassaDb : RoomDatabase() {
    abstract fun getPostDao(): PostDao
}