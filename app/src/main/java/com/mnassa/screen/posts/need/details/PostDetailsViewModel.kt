package com.mnassa.screen.posts.need.details

import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/19/2018.
 */
interface PostDetailsViewModel : MnassaViewModel {
    val postChannel: BroadcastChannel<PostModel>
    val postTagsChannel: BroadcastChannel<List<TagModel>>
    val finishScreenChannel: BroadcastChannel<Unit>
    val scrollToChannel: BroadcastChannel<CommentModel>
    val commentsChannel: BroadcastChannel<List<CommentModel>>
    val canReadCommentsChannel: BroadcastChannel<Boolean>
    val canWriteCommentsChannel: BroadcastChannel<Boolean>

    fun delete()
    fun createComment(text: String, accountsToRecommend: List<String> = emptyList(), replyTo: CommentModel? = null)
    fun deleteComment(commentModel: CommentModel)
    fun repost(sharingOptions: SharingOptionsController.ShareToOptions)
}