package com.mnassa.domain.model

import android.net.Uri
import java.util.*

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentModel : Model {
    val createdAt: Date
    val creator: ShortAccountModel
    val text: String?
    val recommends: List<ShortAccountModel>
    val isRewarded: Boolean
    val images: List<String>
}

interface CommentReplyModel : CommentModel {
    val parentId: String
}

val CommentModel.mostParentCommentId: String
    get() {
        return (this as? CommentReplyModel)?.parentId ?: id
    }

data class RawCommentModel(
        var id: String? = null,
        var postId: String? = null,
        var text: String?,
        var accountsToRecommend: List<String>,
        var uploadedImages: List<String>,
        var imagesToUpload: List<Uri>,
        var parentCommentId: String? = null
)