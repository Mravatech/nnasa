package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.CommentResponseEntity
import com.mnassa.data.network.bean.retrofit.response.CreateCommentResponse
import com.mnassa.data.network.bean.retrofit.response.GetCommentsResponse
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.CommentModelImpl
import com.mnassa.domain.model.impl.CommentReplyModelImpl
import java.util.*

/**
 * Created by Peter on 3/23/2018.
 */
class CommentsConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertGetCommentsResponse)
        convertersContext.registerConverter(this::convertCommentEntity)
        convertersContext.registerConverter(this::convertCreateCommentResponse)
    }

    private fun convertGetCommentsResponse(input: GetCommentsResponse, token: Any?, converter: ConvertersContext): List<CommentModel> {
        if (input.data.infoRestriction == NetworkContract.ErrorCode.NO_RIGHTS_TO_COMMENT) {
            throw NoRightsToComment(input.data.infoRestriction ?: input.toString())
        }

        return input.data.data?.entries?.flatMap { (mainCommentId, commentBody) ->
            commentBody.id = mainCommentId
            converter.convert(commentBody, token, List::class.java) as List<CommentModel>
        } ?: emptyList()
    }

    private fun convertCreateCommentResponse(input: CreateCommentResponse, token: Any?, converter: ConvertersContext): CommentModel {
        if (input.data?.infoRestriction == NetworkContract.ErrorCode.NO_RIGHTS_TO_COMMENT) {
            throw NoRightsToComment(input.data?.infoRestriction ?: input.toString())
        }

        val parentId = token as? String

        return input.data?.comment?.entries?.map { (mainCommentId, commentBody) ->
            commentBody.id = mainCommentId
            commentBody.parentItemId = parentId

            converter.convert(commentBody, token, List::class.java).first() as CommentModel
        }?.first() ?: throw NetworkException(input.toString())
    }

    private fun convertCommentEntity(input: CommentResponseEntity, token: Any?, converter: ConvertersContext): List<CommentModel> {
        val parentItemId = input.parentItemId

        val head: CommentModel = when {
            parentItemId != null -> CommentReplyModelImpl(
                    id = input.id,
                    createdAt = Date(input.createdAt),
                    creator = convertUser(input.creator, converter).first(),
                    text = input.text,
                    recommends = convertUser(input.recommendedAccounts
                            ?: emptyMap(), converter),
                    parentId = parentItemId
            )
            else -> CommentModelImpl(
                    id = input.id,
                    createdAt = Date(input.createdAt),
                    creator = convertUser(input.creator, converter).first(),
                    text = input.text,
                    recommends = convertUser(input.recommendedAccounts ?: emptyMap(), converter)
            )
        }

        val tail = (input.replies ?: emptyMap()).entries.map { (replyId, commentBody) ->
            CommentReplyModelImpl(
                    id = replyId,
                    createdAt = Date(commentBody.createdAt),
                    creator = convertUser(commentBody.creator, converter).first(),
                    text = commentBody.text,
                    recommends = convertUser(commentBody.recommendedAccounts ?: emptyMap(), converter),
                    parentId = head.id
            )
        }

        val result: MutableList<CommentModel> = ArrayList(tail.size + 1)
        result += head
        result += tail
        return result
    }

    private fun convertUser(input: Map<String, ShortAccountDbEntity>, converter: ConvertersContext): List<ShortAccountModel> {
        return input.entries.map { (userId, userBody) ->
            userBody.id = userId
            converter.convert<ShortAccountModel>(userBody)
        }
    }
}