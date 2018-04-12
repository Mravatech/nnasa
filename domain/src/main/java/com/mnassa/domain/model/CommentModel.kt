package com.mnassa.domain.model

import java.util.*

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentModel : Model {
    val createdAt: Date
    val creator: ShortAccountModel
    val text: String?
    val recommends: List<ShortAccountModel>
}

interface CommentReplyModel : CommentModel {
    val parentId: String
}

val CommentModel.mostParentCommentId: String
    get() {
        return (this as? CommentReplyModel)?.parentId ?: id
    }