package com.mnassa.data.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.mnassa.data.database.entity.PostRoomEntity

/**
 * Created by Peter on 7/11/2018.
 */
@Dao
interface PostDao {

//    @Query("""
//        select *
//        from POST
//        where accountId = :accountId and (case when :offset is null then 1 else  id < :offset end)
//        order by id desc
//        limit :limit
//        """)
//    fun getAllByAccountId(accountId: String, limit: Int, offset: String?): List<PostRoomEntity>

//    @Query("""
//        DELETE
//        FROM POST
//        WHERE accountId = :accountId
//    """)
//    fun deleteAllWithAccountId(accountId: String)

    @Query("""
        DELETE
        FROM POST
        where id = :id
    """)
    fun deleteById(id: String)

    @Query("""
        select *
        from POST
        where id = :id
        limit 1
    """)
    fun getById(id: String): PostRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: PostRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<PostRoomEntity>)

}