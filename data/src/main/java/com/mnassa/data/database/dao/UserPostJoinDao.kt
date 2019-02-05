package com.mnassa.data.database.dao

import com.mnassa.data.database.entity.PostRoomEntity
import com.mnassa.data.database.entity.UserRoomEntity
import androidx.room.*
import com.mnassa.data.database.entity.PostForUser
import com.mnassa.data.database.entity.PostUserJoin

@Dao
abstract class UserPostJoinDao {
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    abstract fun insertJoin(userRepoJoin: PostUserJoin)
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    abstract fun insertUser(userRoomEntity: UserRoomEntity)
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    abstract fun insertPost(postRoomEntity: PostRoomEntity)

    @Transaction
    open fun insert(postForUser: PostForUser){
        val postId = postForUser.postRoomEntity.id
        val userId = postForUser.userRoomEntity.id
        insertPost(postForUser.postRoomEntity)
        insertUser(postForUser.userRoomEntity)
        insertJoin(PostUserJoin(userId, postId))
    }

    @Transaction
    open fun getPostAndRemove(id: String, userId: String):PostRoomEntity? {
        val post = getPostById(id)
        post?.let {
            deleteJoin(userId, it.id)
        }
        return post
    }

    @Query("""
        select *
        from POST
        where id = :id
        limit 1
    """)
    abstract fun getPostById(id: String): PostRoomEntity?

    @Query("""
        DELETE
        FROM POST
        where id = :id
    """)
    abstract fun deletePostById(id: String)

    @Query("""
        DELETE
        FROM USER_POST_JOIN
        where userId = :userId AND postId = :postId
    """)
    abstract fun deleteJoin(userId: String, postId: String)

    @Query("""
        SELECT *
        FROM POST
        INNER JOIN USER_POST_JOIN
        ON POST.id=USER_POST_JOIN.postId
        WHERE USER_POST_JOIN.userId=:userId
        """)
    abstract fun loadPostsByUserId(userId: String): List<PostRoomEntity>

    @Transaction
    open fun clearAll() {
        clearJoin()
        clearPost()
        clearUser()
    }

    @Query("""DELETE FROM USER_POST_JOIN""")
    abstract fun clearJoin()

    @Query("""DELETE FROM USER""")
    abstract fun clearUser()

    @Query("""DELETE FROM POST""")
    abstract fun clearPost()

}