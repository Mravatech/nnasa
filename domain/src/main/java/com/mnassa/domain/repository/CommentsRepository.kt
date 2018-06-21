package com.mnassa.domain.repository

import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RawCommentModel

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentsRepository {
    suspend fun getCommentsByPost(postId: String): List<CommentModel>
    suspend fun getCommentsByEvent(eventId: String): List<CommentModel>
    //
    suspend fun writePostComment(comment: RawCommentModel): CommentModel
    suspend fun writeEventComment(comment: RawCommentModel): CommentModel
    //
    suspend fun replyToPostComment(comment: RawCommentModel): CommentModel
    suspend fun replyToEventComment(comment: RawCommentModel): CommentModel
    //
    suspend fun deletePostComment(comment: CommentModel)
    suspend fun deleteEventComment(comment: CommentModel)
    //
    suspend fun editPostComment(comment: RawCommentModel)
    suspend fun editEventComment(comment: RawCommentModel)
}