package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.mnassa.data.network.api.FirebasePostApi
import com.mnassa.data.network.bean.retrofit.request.GetCommentsRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
class CommentsRepositoryImpl(private val converter: ConvertersContext,
                             private val postApi: FirebasePostApi,
                             private val exceptionHandler: ExceptionHandler) : CommentsRepository {

    override suspend fun getCommentsByPost(postId: String): List<CommentModel> {
        val result = postApi.getComments(GetCommentsRequest(postId)).handleException(exceptionHandler)
        return converter.convert(result)
    }
}