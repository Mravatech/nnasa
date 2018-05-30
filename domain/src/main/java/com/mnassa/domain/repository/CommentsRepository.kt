package com.mnassa.domain.repository

import com.mnassa.domain.model.CommentModel

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentsRepository {
    suspend fun getCommentsByPost(postId: String): List<CommentModel>
    suspend fun writePostComment(postId: String, text: String?, accountsToRecommend: List<String>): CommentModel
    suspend fun replyToPostComment(postId: String, commentId: String, text: String, accountsToRecommend: List<String>): CommentModel
    suspend fun deletePostComment(comment: CommentModel)
    suspend fun deleteEventComment(comment: CommentModel)
    suspend fun editPostComment(
            originalCommentId: String,
            text: String?,
            accountsToRecommend: List<String>,
            parentCommentId: String?)

    suspend fun editEventComment(
            originalCommentId: String,
            text: String?,
            accountsToRecommend: List<String>,
            parentCommentId: String?)

    //events
    suspend fun getCommentsByEvent(eventId: String): List<CommentModel>

    suspend fun writeEventComment(eventId: String, text: String?, accountsToRecommend: List<String>): CommentModel
    suspend fun replyToEventComment(eventId: String, commentId: String, text: String, accountsToRecommend: List<String>): CommentModel
}