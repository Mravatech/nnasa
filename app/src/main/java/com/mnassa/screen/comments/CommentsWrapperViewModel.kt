package com.mnassa.screen.comments

import com.mnassa.domain.model.CommentModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/17/2018.
 */
interface CommentsWrapperViewModel : MnassaViewModel {
    val scrollToChannel: BroadcastChannel<CommentModel>
    val commentsChannel: BroadcastChannel<List<CommentModel>>
    val canReadCommentsChannel: BroadcastChannel<Boolean>
    val canWriteCommentsChannel: BroadcastChannel<Boolean>

    fun createComment(text: String, accountsToRecommend: List<String> = emptyList(), replyTo: CommentModel? = null)
    fun editComment(originalComment: CommentModel, text: String, accountsToRecommend: List<String> = emptyList(), replyTo: CommentModel? = null)
    fun deleteComment(commentModel: CommentModel)
}