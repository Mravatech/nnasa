package com.mnassa.data.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.mnassa.data.database.entity.PostRoomEntity

/**
 * Created by Peter on 7/11/2018.
 */
@Dao
interface PostDao {

    @Query("""
        select *
        from POST
        where accountId = :accountId and (case when :offset is null then 1 else  id < :offset end)
        order by id desc
        limit :limit
        """)
    fun getAllByAccountId(accountId: String, limit: Int, offset: String?): List<PostRoomEntity>

    @Query("""
        select id
        from POST
        where accountId = :accountId
        order by id desc
    """)
    fun getIndexByAccountId(accountId: String): LiveData<List<String>>

    /*
    select *
        from POST
        where accountId = :accountId and (case when :offset is null then 1 else  id < :offset end)
        order by id desc
        limit :limit
     */

    @Insert
    fun insert(entity: PostRoomEntity)

    @Insert
    fun insert(entities: List<PostRoomEntity>)

}