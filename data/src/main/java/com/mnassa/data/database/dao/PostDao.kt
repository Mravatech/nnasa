package com.mnassa.data.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.mnassa.data.database.entity.PostRoomEntity

/**
 * Created by Peter on 7/11/2018.
 */
@Dao
interface PostDao {

    @Query("select * from POST")
    fun getAll(): List<PostRoomEntity>
}