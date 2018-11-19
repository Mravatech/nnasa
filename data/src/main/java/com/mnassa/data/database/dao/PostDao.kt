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

    @Query("""
       select *
       from POST
       where userId = :currentUserId
       ORDER BY createdAt
       DESC
    """
    )
    fun getAllPosts(currentUserId: String): List<PostRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: PostRoomEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<PostRoomEntity>)

    @Query("""DELETE FROM POST""")
    fun clearAll()

}