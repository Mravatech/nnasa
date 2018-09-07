package com.mnassa.data.repository

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.convert
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseCommentsApi
import com.mnassa.data.network.bean.retrofit.request.CreateCommentRequest
import com.mnassa.data.network.bean.retrofit.request.DeleteCommentRequest
import com.mnassa.data.network.bean.retrofit.request.EditCommentRequest
import com.mnassa.data.network.bean.retrofit.request.GetCommentsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.CommentReplyModel
import com.mnassa.domain.model.RawCommentModel
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
class CommentsRepositoryImpl(private val converter: ConvertersContext,
                             private val commentsApi: FirebaseCommentsApi,
                             private val exceptionHandler: ExceptionHandler) : CommentsRepository {

    override suspend fun getCommentsByPost(postId: String): List<CommentModel> {
        val result = commentsApi.getComments(GetCommentsRequest(postId = postId, entityType = NetworkContract.EntityType.POST)).handleException(exceptionHandler)
        return converter.convert(result)
    }

    override suspend fun writePostComment(comment: RawCommentModel): CommentModel {
        val result = commentsApi.createComment(CreateCommentRequest(
                postId = requireNotNull(comment.postId),
                text = comment.text,
                accountIds = comment.accountsToRecommend,
                entityType = NetworkContract.EntityType.POST,
                parentCommentId = null,
                images = comment.uploadedImages.takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
        return converter.convert(result, Unit, CommentModel::class.java)
    }

    override suspend fun replyToPostComment(comment: RawCommentModel): CommentModel {
        val result = commentsApi.createComment(CreateCommentRequest(
                postId = requireNotNull(comment.postId),
                text = comment.text,
                accountIds = comment.accountsToRecommend,
                entityType = NetworkContract.EntityType.POST,
                parentCommentId = comment.parentCommentId,
                images = comment.uploadedImages
        )).handleException(exceptionHandler)
        return converter.convert(result, comment.parentCommentId, CommentModel::class.java)
    }

    override suspend fun deletePostComment(comment: CommentModel) {
        commentsApi.deleteComment(DeleteCommentRequest(
                commentId = comment.id,
                entityType = NetworkContract.EntityType.POST,
                parentCommentId = (comment as? CommentReplyModel)?.parentId
        )).handleException(exceptionHandler)
    }

    override suspend fun deleteEventComment(comment: CommentModel) {
        commentsApi.deleteComment(DeleteCommentRequest(
                commentId = comment.id,
                entityType = NetworkContract.EntityType.EVENT,
                parentCommentId = (comment as? CommentReplyModel)?.parentId
        )).handleException(exceptionHandler)
    }

    override suspend fun editPostComment(comment: RawCommentModel) {
        commentsApi.editComment(EditCommentRequest(
                commentId = requireNotNull(comment.id),
                text = comment.text,
                entityType = NetworkContract.EntityType.POST,
                accountIds = comment.accountsToRecommend,
                parentCommentId = comment.parentCommentId,
                images = comment.uploadedImages.takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
    }

    override suspend fun editEventComment(comment: RawCommentModel) {
        commentsApi.editComment(EditCommentRequest(
                commentId = requireNotNull(comment.id),
                text = comment.text,
                entityType = NetworkContract.EntityType.EVENT,
                accountIds = comment.accountsToRecommend,
                parentCommentId = comment.parentCommentId,
                images = comment.uploadedImages.takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    override suspend fun getCommentsByEvent(eventId: String): List<CommentModel> {
        val result = commentsApi.getComments(GetCommentsRequest(
                postId = eventId,
                entityType = NetworkContract.EntityType.EVENT)).handleException(exceptionHandler)
        return converter.convert(result)
    }

    override suspend fun writeEventComment(comment: RawCommentModel): CommentModel {
        val result = commentsApi.createComment(CreateCommentRequest(
                postId = requireNotNull(comment.postId),
                text = comment.text,
                accountIds = comment.accountsToRecommend,
                entityType = NetworkContract.EntityType.EVENT,
                parentCommentId = null,
                images = comment.uploadedImages.takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
        return converter.convert(result, Unit, CommentModel::class.java)
    }

    override suspend fun replyToEventComment(comment: RawCommentModel): CommentModel {
        val result = commentsApi.createComment(CreateCommentRequest(
                postId = requireNotNull(comment.postId),
                text = comment.text,
                accountIds = comment.accountsToRecommend,
                entityType = NetworkContract.EntityType.EVENT,
                parentCommentId = comment.parentCommentId,
                images = comment.uploadedImages.takeIf { it.isNotEmpty() }
        )).handleException(exceptionHandler)
        return converter.convert(result, comment.parentCommentId, CommentModel::class.java)
    }
}