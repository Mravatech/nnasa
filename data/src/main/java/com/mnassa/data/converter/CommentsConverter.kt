package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.GetCommentsResponse
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
        convertersContext.registerConverter(this::convertComment)
    }

    private fun convertComment(input: GetCommentsResponse, token: Any?, converter: ConvertersContext): List<CommentModel> {
        return input.data.data.entries.flatMap { (mainCommentId, commentBody) ->
            val head = CommentModelImpl(
                    id = mainCommentId,
                    createdAt = Date(commentBody.createdAt),
                    creator = convertUser(commentBody.creator, converter).first(),
                    text = commentBody.text,
                    recommends = convertUser(commentBody.recommendedAccounts ?: emptyMap(), converter)
            )

            val tail = (commentBody.replies ?: emptyMap()).map { (commentId, commentBody) ->
                CommentReplyModelImpl(
                        id = commentId,
                        createdAt = Date(commentBody.createdAt),
                        creator = convertUser(commentBody.creator, converter).first(),
                        text = commentBody.text,
                        recommends = convertUser(commentBody.recommendedAccounts ?: emptyMap(), converter),
                        parentId = mainCommentId
                )
            }

            val result = ArrayList<CommentModel>(tail.size + 1)
            result += head
            result += tail
            return result
        }
    }

    private fun convertUser(input: Map<String, ShortAccountDbEntity>, converter: ConvertersContext): List<ShortAccountModel> {
        return input.entries.map { (userId, userBody) ->
            userBody.id = userId
            converter.convert<ShortAccountModel>(userBody)
        }
    }
}