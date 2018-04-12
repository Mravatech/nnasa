package com.mnassa.data.network.bean.firebase

import android.support.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 3/23/2018.
 */
internal data class CommentDbEntity(
        @SerializedName("id")
        @Nullable
        override var id: String,
        @SerializedName("createdAt") var createdAt: Long,
        @SerializedName("creator") var creator: ShortAccountDbEntity,
        @SerializedName("isPrivate") var isPrivate: Boolean,
        @SerializedName("itemAuthorAid") var itemAuthorAid: String,
        @SerializedName("text") var text: String
) : HasId

internal data class ReplyCommentDbEntity(
        @SerializedName("id")
        @Nullable
        override var id: String,
        @SerializedName("createdAt") var createdAt: Long,
        @SerializedName("creator") var creator: ShortAccountDbEntity,
        @SerializedName("isPrivate") var isPrivate: Boolean,
        @SerializedName("itemAuthorAid") var itemAuthorAid: String,
        @SerializedName("text") var text: String,
        @SerializedName("parentItem") var parentItem: CommentDbEntity
) : HasId