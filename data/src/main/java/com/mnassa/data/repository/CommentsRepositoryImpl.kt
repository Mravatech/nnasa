package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseCommentsApi
import com.mnassa.data.network.bean.retrofit.request.CreateCommentRequest
import com.mnassa.data.network.bean.retrofit.request.GetCommentsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
class CommentsRepositoryImpl(private val converter: ConvertersContext,
                             private val commentsApi: FirebaseCommentsApi,
                             private val exceptionHandler: ExceptionHandler) : CommentsRepository {

    override suspend fun getCommentsByPost(postId: String): List<CommentModel> {
        val result = commentsApi.getComments(GetCommentsRequest(postId)).handleException(exceptionHandler)
        return converter.convert(result)
    }

    override suspend fun writePostComment(postId: String, text: String?, accountsToRecommend: List<String>): CommentModel {
        val result = commentsApi.createComment(CreateCommentRequest(
                postId = postId,
                text = text,
                accountIds = accountsToRecommend,
                entityType = NetworkContract.ItemType.POST,
                commentId = null
        )).handleException(exceptionHandler)
        return converter.convert(result, Unit, CommentModel::class.java)
    }

    override suspend fun replyToPostComment(postId: String, commentId: String, text: String, accountsToRecommend: List<String>): CommentModel {
        val result = commentsApi.createComment(CreateCommentRequest(
                postId = postId,
                text = text,
                accountIds = accountsToRecommend,
                entityType = NetworkContract.ItemType.POST,
                commentId = commentId
        )).handleException(exceptionHandler)
        return converter.convert(result, commentId, CommentModel::class.java)
    }

    override suspend fun deleteComment(commentId: String) {
        commentsApi.deleteComment(commentId).handleException(exceptionHandler)
    }
}