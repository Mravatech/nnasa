package com.mnassa.screen.comments

import android.net.Uri
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RawCommentModel
import com.mnassa.domain.model.RewardModel
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

    fun createComment(comment: RawCommentModel)
    fun editComment(comment: RawCommentModel)
    fun deleteComment(commentModel: CommentModel)
    fun sendPointsForComment(rewardModel: RewardModel) {}
    fun preloadImage(imageFile: Uri)
}