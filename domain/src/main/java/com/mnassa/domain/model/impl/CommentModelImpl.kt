package com.mnassa.domain.model.impl

import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.CommentReplyModel
import com.mnassa.domain.model.ShortAccountModel
import java.util.*

/**
 * Created by Peter on 3/23/2018.
 */
data class CommentModelImpl(
        override var id: String,
        override val createdAt: Date,
        override val creator: ShortAccountModel,
        override val text: String?,
        override val recommends: List<ShortAccountModel>
) : CommentModel {
}

data class CommentReplyModelImpl(
        override var id: String,
        override val createdAt: Date,
        override val creator: ShortAccountModel,
        override val text: String?,
        override val recommends: List<ShortAccountModel>,
        override val parentId: String
) : CommentReplyModel