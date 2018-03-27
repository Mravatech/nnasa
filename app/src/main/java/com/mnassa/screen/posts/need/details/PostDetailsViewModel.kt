package com.mnassa.screen.posts.need.details

import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/19/2018.
 */
interface PostDetailsViewModel : MnassaViewModel {
    val postChannel: BroadcastChannel<Post>
    val postTagsChannel: BroadcastChannel<List<TagModel>>
    val finishScreenChannel: BroadcastChannel<Unit>
    val scrollToChannel: BroadcastChannel<CommentModel>
    val commentsChannel: BroadcastChannel<List<CommentModel>>

    fun delete()
    fun createComment(text: String, accountsToRecommend: List<String> = emptyList(), replyTo: CommentModel? = null)
    fun deleteComment(commentModel: CommentModel)
}