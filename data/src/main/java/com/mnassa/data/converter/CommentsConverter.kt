package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.convert
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.extensions.isSuppressed
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
import timber.log.Timber
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
            try {
                commentBody.id = mainCommentId
                converter.convert(commentBody, token, List::class.java) as List<CommentModel>
            } catch (e: Exception) {
                Timber.e(e)

                //ignore server side errors & try to load only valid comment models
                if (e.isSuppressed) emptyList<CommentModel>() else throw e
            }
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
                    creator = convertUser(input.creator, converter).firstOrNull() ?: ShortAccountModel.EMPTY,
                    text = input.text,
                    recommends = convertUser(input.recommendedAccounts
                            ?: emptyMap(), converter),
                    parentId = parentItemId,
                    isRewarded = input.isRewarded,
                    images = input.images ?: emptyList()
            )
            else -> CommentModelImpl(
                    id = input.id,
                    createdAt = Date(input.createdAt),
                    creator = convertUser(input.creator, converter).firstOrNull() ?: ShortAccountModel.EMPTY,
                    text = input.text,
                    recommends = convertUser(input.recommendedAccounts ?: emptyMap(), converter),
                    isRewarded = input.isRewarded,
                    images = input.images ?: emptyList()
            )
        }

        val tail = (input.replies ?: emptyMap()).entries.mapNotNull { (replyId, commentBody) ->
            try {
                CommentReplyModelImpl(
                        id = replyId,
                        createdAt = Date(commentBody.createdAt),
                        creator = convertUser(commentBody.creator, converter).firstOrNull() ?: ShortAccountModel.EMPTY,
                        text = commentBody.text,
                        recommends = convertUser(commentBody.recommendedAccounts
                                ?: emptyMap(), converter),
                        parentId = head.id,
                        isRewarded = commentBody.isRewarded,
                        images = commentBody.images ?: emptyList()
                )
            } catch (e: Exception) {
                Timber.e(e)
                //ignore server side errors & try to load valid comment models
                null
            }
        }

        val result: MutableList<CommentModel> = ArrayList(tail.size + 1)
        result += head
        result += tail
        return result
    }

    private fun convertUser(input: Map<String, ShortAccountDbEntity?>?, converter: ConvertersContext): List<ShortAccountModel> {
        if (input == null) return emptyList()
        return input.entries.map { (userId, userBody) ->
            if (userBody == null) ShortAccountModel.EMPTY
            else {
                userBody.id = userId
                converter.convert(userBody)
            }
        }
    }
}