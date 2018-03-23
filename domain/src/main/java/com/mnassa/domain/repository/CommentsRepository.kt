package com.mnassa.domain.repository

import com.mnassa.domain.model.CommentModel

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentsRepository {
    suspend fun getCommentsByPost(postId: String): List<CommentModel>
}