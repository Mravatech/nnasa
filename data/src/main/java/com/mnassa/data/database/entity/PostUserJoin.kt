package com.mnassa.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

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