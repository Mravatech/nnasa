package com.mnassa.data.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index

@Entity(tableName = PostUserJoin.TABLE_NAME,
        primaryKeys = ["userId", "postId"],
        foreignKeys = [ForeignKey(entity = UserRoomEntity::class,
                parentColumns = ["id"],
                childColumns = ["userId"]),
            ForeignKey(entity = PostRoomEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["postId"])],
                indices = [(Index("userId")), (Index("postId"))])
class PostUserJoin(
        @ColumnInfo(name = "userId")
        val userId: String,
        @ColumnInfo(name = "postId")
        val postId: String
) {
    companion object {
        const val TABLE_NAME = "USER_POST_JOIN"
    }
}